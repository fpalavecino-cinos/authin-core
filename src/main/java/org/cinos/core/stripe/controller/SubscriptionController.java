package org.cinos.core.stripe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.cinos.core.stripe.dto.*;
import org.cinos.core.stripe.service.StripeService;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.model.Role;
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.posts.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("subscriptions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SubscriptionController {

    private final StripeService stripeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    /**
     * Obtiene los planes de suscripción disponibles
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanDto>> getSubscriptionPlans() {
        try {
            List<SubscriptionPlanDto> plans = stripeService.getSubscriptionPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crea una nueva suscripción
     */
    @PostMapping("/create")
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        try {
            if (request.getPlanId() == null || request.getPlanId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Plan ID es requerido")
                                .success(false)
                                .build());
            }

            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            String userId = userEntity.getId().toString();
            String email = userEntity.getEmail();

            StripeService.StripeSubscriptionResult result = stripeService.createSubscriptionWithId(
                request.getPlanId(),
                userId,
                email,
                request.isTrial()
            );
            userEntity.setStripeSubscriptionId(result.subscriptionId);
            userRepository.save(userEntity);
            return ResponseEntity.ok(SubscriptionResponse.builder().clientSecret(result.clientSecret).success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder()
                            .message("Error: " + e.getMessage())
                            .success(false)
                            .build());
        }
    }

    /**
     * Obtiene los detalles de la suscripción del usuario
     */
    @GetMapping("/details")
    public ResponseEntity<SubscriptionResponse> getSubscriptionDetails() {
        try {
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            
            if (userEntity.getStripeSubscriptionId() == null || userEntity.getStripeSubscriptionId().isEmpty()) {
                return ResponseEntity.ok(SubscriptionResponse.builder()
                        .message("No tienes una suscripción activa")
                        .success(false)
                        .build());
            }
            
            // Obtener información de la suscripción desde Stripe
            String status = stripeService.getSubscriptionStatus(userEntity.getId().toString());
            Long nextRenewal = stripeService.getSubscriptionNextRenewal(userEntity.getStripeSubscriptionId());
            
            String message;
            if ("canceled".equals(status)) {
                message = "Estado: Cancelada (activa hasta el final del período), Próxima renovación: " + new java.util.Date(nextRenewal * 1000);
            } else {
                message = "Estado: " + status + ", Próxima renovación: " + new java.util.Date(nextRenewal * 1000);
            }
            
            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .message(message)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Cancela una suscripción
     */
    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription() {
        try {
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            
            if (userEntity.getStripeSubscriptionId() == null || userEntity.getStripeSubscriptionId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("No tienes una suscripción activa para cancelar")
                                .success(false)
                                .build());
            }

            // Cancelar suscripción en Stripe (solo marca para cancelar al final del período)
            stripeService.cancelSubscription(userEntity.getStripeSubscriptionId());
            
            // NO eliminar el rol premium inmediatamente - se mantendrá hasta el final del período
            // El rol premium se eliminará automáticamente cuando Stripe envíe el webhook de cancelación
            // o cuando el período actual termine
            
            return ResponseEntity.ok(
                    SubscriptionResponse.builder()
                            .message("Suscripción cancelada exitosamente. Tu acceso premium se mantendrá hasta el final del período actual.")
                            .success(true)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Obtiene el estado de la suscripción actual del usuario
     */
    @GetMapping("/status")
    public ResponseEntity<SubscriptionResponse> getSubscriptionStatus() {
        try {
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            
            boolean isPremium = userEntity.getRoles() != null && userEntity.getRoles().contains(Role.PREMIUM);
            String status = isPremium ? "premium" : "free";
            
            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .message(status)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Verifica el estado de un pago
     */
    @GetMapping("/payment/{paymentIntentId}/status")
    public ResponseEntity<SubscriptionResponse> getPaymentStatus(@PathVariable String paymentIntentId) {
        try {
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Payment Intent ID es requerido")
                                .success(false)
                                .build());
            }

            String status = stripeService.checkPaymentStatus(paymentIntentId);
            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .message("Estado del pago: " + status)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Confirma un pago exitoso
     */
    @PostMapping("/payment/{paymentIntentId}/confirm")
    public ResponseEntity<SubscriptionResponse> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Payment Intent ID es requerido")
                                .success(false)
                                .build());
            }

            boolean confirmed = stripeService.confirmPayment(paymentIntentId);
            if (confirmed) {
                return ResponseEntity.ok(
                        SubscriptionResponse.builder()
                                .message("Pago confirmado exitosamente")
                                .success(true)
                                .build());
            } else {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("El pago no se pudo confirmar")
                                .success(false)
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Crea una sesión de Stripe Checkout para suscripción
     */
    @PostMapping("/checkout-session")
    public ResponseEntity<SubscriptionResponse> createCheckoutSession(@RequestBody CreateSubscriptionRequest request) {
        try {
            if (request.getPlanId() == null || request.getPlanId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Plan ID es requerido")
                                .success(false)
                                .build());
            }
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            String email = userEntity.getEmail();
            // PriceId real de Stripe
            String priceId = request.getPlanId();
            String successUrl = request.getSuccessUrl();
            String cancelUrl = request.getCancelUrl();
            String url = stripeService.createSubscriptionCheckoutSession(priceId, successUrl, cancelUrl, email);
            return ResponseEntity.ok(SubscriptionResponse.builder() .checkoutUrl(url).success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Crea una sesión de Stripe Checkout para acceso a verificación técnica
     */
    @PostMapping("/verification-access-checkout")
    public ResponseEntity<SubscriptionResponse> createVerificationAccessCheckoutSession(
            @RequestBody BuyVerificationAccessRequest request) {
        try {
            if (request.postId() == null) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Post ID es requerido")
                                .success(false)
                                .build());
            }

            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();

            String successUrl = "http://localhost:8100/verification-details/" + request.postId() + "?justBought=true";
            String cancelUrl = "http://localhost:8100/verification-details/" + request.postId();

            String checkoutUrl = stripeService.createVerificationAccessCheckoutSession(
                request.postId(), 
                userEntity, 
                successUrl, 
                cancelUrl
            );

            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder()
                            .message("Error: " + e.getMessage())
                            .success(false)
                            .build());
        }
    }

    /**
     * Endpoint de prueba para simular la actualización a premium
     */
    @PostMapping("/test-upgrade/{email}")
    public ResponseEntity<String> testUpgradeToPremium(@PathVariable String email) {
        try {
            UserEntity user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                System.out.println("Testing upgrade for user: " + user.getEmail() + " - Current roles: " + user.getRoles());
                
                if (user.getRoles() == null || !user.getRoles().contains(Role.PREMIUM)) {
                    if (user.getRoles() == null) {
                        user.setRoles(new java.util.ArrayList<>());
                    }
                    user.getRoles().add(Role.PREMIUM);
                    userRepository.save(user);
                    System.out.println("User upgraded to PREMIUM successfully: " + user.getEmail());
                    return ResponseEntity.ok("User upgraded to PREMIUM successfully: " + user.getEmail());
                } else {
                    System.out.println("User already has PREMIUM role: " + user.getEmail());
                    return ResponseEntity.ok("User already has PREMIUM role: " + user.getEmail());
                }
            } else {
                System.err.println("User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found for email: " + email);
            }
        } catch (Exception e) {
            System.err.println("Error in test upgrade: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint de prueba para simular el webhook payment_intent.succeeded
     */
    @PostMapping("/test-verification-access/{postId}/{userId}")
    public ResponseEntity<String> testVerificationAccessWebhook(
            @PathVariable Long postId,
            @PathVariable Long userId) {
        try {
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                // Desbloquear acceso a la verificación específica
                org.cinos.core.posts.entity.PostEntity post = postRepository.findById(postId).orElse(null);
                if (post != null) {
                    if (!user.getUnlockedTechnicalVerifications().contains(post)) {
                        user.getUnlockedTechnicalVerifications().add(post);
                        userRepository.save(user);
                        System.out.println("🔓 [TEST] Acceso a verificación desbloqueado para usuario: " + user.getEmail() + " y post: " + postId);
                        return ResponseEntity.ok("Acceso desbloqueado exitosamente para usuario: " + user.getEmail() + " y post: " + postId);
                    } else {
                        System.out.println("ℹ️ [TEST] Usuario ya tenía acceso a esta verificación: " + user.getEmail() + " y post: " + postId);
                        return ResponseEntity.ok("Usuario ya tenía acceso a esta verificación");
                    }
                } else {
                    System.err.println("❌ [TEST] Post no encontrado con ID: " + postId);
                    return ResponseEntity.badRequest().body("Post no encontrado con ID: " + postId);
                }
            } else {
                System.err.println("❌ [TEST] Usuario no encontrado con ID: " + userId);
                return ResponseEntity.badRequest().body("Usuario no encontrado con ID: " + userId);
            }
        } catch (Exception e) {
            System.err.println("❌ [TEST] Error al procesar acceso a verificación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Webhook de Stripe para eventos de suscripción
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws JsonProcessingException {
        System.out.println("[DEBUG] Payload length: " + payload.length());
        System.out.println("[DEBUG] Signature: " + sigHeader);
        System.out.println("[DEBUG] endpointSecret: " + endpointSecret);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println("✅ Webhook verificado: " + event.getType());
        } catch (Exception e) {
            System.err.println("❌ Firma inválida: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        String eventType = event.getType();

        if ("checkout.session.completed".equals(eventType)) {
            System.out.println("➡️ Evento: checkout.session.completed");

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Session session = null;
            Map<String, Object> sessionMap = null;
            if (deserializer.getObject().isPresent()) {
                session = (Session) deserializer.getObject().get();
            } else {
                // Deserialización manual a Map si el SDK no puede
                String rawJson = deserializer.getRawJson();
                System.err.println("No se pudo deserializar el objeto Session. JSON crudo: " + rawJson);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    sessionMap = mapper.readValue(rawJson, Map.class);
                } catch (Exception ex) {
                    System.err.println("Error al mapear manualmente el Session: " + ex.getMessage());
                    return ResponseEntity.badRequest().body("No se pudo deserializar el objeto Session");
                }
            }
            if (session != null) {
                String email = session.getCustomerDetails() != null ? session.getCustomerDetails().getEmail() : session.getCustomerEmail();
                System.out.println("Email cliente: " + email);
                // Verificar si es un pago de acceso a verificación
                String type = session.getMetadata().get("type");
                if ("verification_access".equals(type)) {
                    String postId = session.getMetadata().get("postId");
                    String userId = session.getMetadata().get("userId");
                    System.out.println("🔍 Procesando checkout de acceso a verificación - PostId: " + postId + ", UserId: " + userId);
                    try {
                        Long postIdLong = null, userIdLong = null;
                        try {
                            postIdLong = Long.parseLong(postId);
                            userIdLong = Long.parseLong(userId);
                        } catch (NumberFormatException e) {
                            System.err.println("Error al convertir postId o userId a Long");
                            return ResponseEntity.badRequest().body("IDs inválidos");
                        }
                        UserEntity user = userRepository.findById(userIdLong).orElse(null);
                        if (user != null) {
                            org.cinos.core.posts.entity.PostEntity post = postRepository.findById(postIdLong).orElse(null);
                            if (post != null) {
                                if (!user.getUnlockedTechnicalVerifications().contains(post)) {
                                    user.getUnlockedTechnicalVerifications().add(post);
                                    userRepository.save(user);
                                    System.out.println("🔓 Acceso a verificación desbloqueado para usuario: " + user.getEmail() + " y post: " + postId);
                                } else {
                                    System.out.println("ℹ️ Usuario ya tenía acceso a esta verificación: " + user.getEmail() + " y post: " + postId);
                                }
                            } else {
                                System.err.println("❌ Post no encontrado con ID: " + postId);
                            }
                        } else {
                            System.err.println("❌ Usuario no encontrado con ID: " + userId);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error al procesar acceso a verificación: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ℹ️ Tipo de checkout no manejado: " + type);
                }
            } else if (sessionMap != null) {
                // Acceso manual a los metadatos
                Map<String, Object> metadata = (Map<String, Object>) sessionMap.get("metadata");
                String type = (String) metadata.get("type");
                if ("verification_access".equals(type)) {
                    String postId = (String) metadata.get("postId");
                    String userId = (String) metadata.get("userId");
                    System.out.println("🔍 Procesando checkout de acceso a verificación (MAP) - PostId: " + postId + ", UserId: " + userId);
                    try {
                        Long postIdLong = null, userIdLong = null;
                        try {
                            postIdLong = Long.parseLong(postId);
                            userIdLong = Long.parseLong(userId);
                        } catch (NumberFormatException e) {
                            System.err.println("Error al convertir postId o userId a Long (MAP)");
                            return ResponseEntity.badRequest().body("IDs inválidos (MAP)");
                        }
                        UserEntity user = userRepository.findById(userIdLong).orElse(null);
                        if (user != null) {
                            org.cinos.core.posts.entity.PostEntity post = postRepository.findById(postIdLong).orElse(null);
                            if (post != null) {
                                if (!user.getUnlockedTechnicalVerifications().contains(post)) {
                                    user.getUnlockedTechnicalVerifications().add(post);
                                    userRepository.save(user);
                                    System.out.println("🔓 Acceso a verificación desbloqueado para usuario: " + user.getEmail() + " y post: " + postId);
                                } else {
                                    System.out.println("ℹ️ Usuario ya tenía acceso a esta verificación: " + user.getEmail() + " y post: " + postId);
                                }
                            } else {
                                System.err.println("❌ Post no encontrado con ID: " + postId);
                            }
                        } else {
                            System.err.println("❌ Usuario no encontrado con ID: " + userId);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error al procesar acceso a verificación (MAP): " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ℹ️ Tipo de checkout no manejado (MAP): " + type);
                }
            } else {
                System.err.println("❌ Session es null incluso tras deserialización manual");
            }

        } else if ("invoice.payment_succeeded".equals(eventType) || "invoice_payment.paid".equals(eventType)) {
            System.out.println("➡️ Evento: " + eventType);

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            com.stripe.model.Invoice invoice = null;
            Map<String, Object> invoiceMap = null;
                if (deserializer.getObject().isPresent()) {
                    Object dataObject = deserializer.getObject().get();
                    if (dataObject instanceof com.stripe.model.Invoice) {
                    invoice = (com.stripe.model.Invoice) dataObject;
                    }
                } else {
                // Deserialización manual a Map si el SDK no puede
                    String rawJson = deserializer.getRawJson();
                System.err.println("No se pudo deserializar el objeto Invoice. JSON crudo: " + rawJson);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    invoiceMap = mapper.readValue(rawJson, Map.class);
                } catch (Exception ex) {
                    System.err.println("Error al mapear manualmente el Invoice: " + ex.getMessage());
                    return ResponseEntity.badRequest().body("No se pudo deserializar el objeto Invoice");
                }
            }

            String invoiceId = null;
            if (invoice != null) {
                invoiceId = invoice.getId();
            } else if (invoiceMap != null) {
                invoiceId = (String) invoiceMap.get("id");
            }

            if (invoiceId != null) {
                try {
                    if (invoice == null) {
                        invoice = com.stripe.model.Invoice.retrieve(invoiceId);
                    }
                    String customerId = invoice.getCustomer();
                    com.stripe.model.Customer customer = com.stripe.model.Customer.retrieve(customerId);
                    String email = customer.getEmail();

                    System.out.println("📧 Email del cliente: " + email);

                    UserEntity user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        System.out.println("👤 Usuario encontrado: " + user.getEmail());
                        if (user.getRoles() == null || !user.getRoles().contains(Role.PREMIUM)) {
                            if (user.getRoles() == null) {
                                user.setRoles(new ArrayList<>());
                            }
                            user.setStripeSubscriptionId(invoice.getSubscription());
                            user.getRoles().add(Role.PREMIUM);
                        }
                        user.setTechnicalVerificationCredits(1); // Resetear créditos
                        userRepository.save(user);
                        System.out.println("🚀 Usuario actualizado a PREMIUM y créditos reseteados: " + user.getEmail());
                    } else {
                        System.err.println("❌ Usuario no encontrado con email: " + email);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al recuperar invoice/customer: " + e.getMessage());
                }
            } else {
                System.err.println("⚠️ No se pudo obtener invoice ID");
            }

        } else if ("payment_intent.succeeded".equals(eventType)) {
            System.out.println("➡️ Evento: payment_intent.succeeded");
            
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            com.stripe.model.PaymentIntent paymentIntent = null;
            if (deserializer.getObject().isPresent()) {
                Object dataObject = deserializer.getObject().get();
                if (dataObject instanceof com.stripe.model.PaymentIntent) {
                    paymentIntent = (com.stripe.model.PaymentIntent) dataObject;
                }
            } else {
                // Deserialización manual si el SDK no puede
                String rawJson = deserializer.getRawJson();
                System.err.println("No se pudo deserializar el objeto PaymentIntent. JSON crudo: " + rawJson);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    paymentIntent = mapper.readValue(rawJson, com.stripe.model.PaymentIntent.class);
                } catch (Exception ex) {
                    System.err.println("Error al mapear manualmente el PaymentIntent: " + ex.getMessage());
                    return ResponseEntity.badRequest().body("No se pudo deserializar el objeto PaymentIntent");
                }
            }

            if (paymentIntent != null) {
                String type = paymentIntent.getMetadata().get("type");
                if ("verification_access".equals(type)) {
                    String postId = paymentIntent.getMetadata().get("postId");
                    String userId = paymentIntent.getMetadata().get("userId");
                    
                    System.out.println("🔍 Procesando pago de acceso a verificación - PostId: " + postId + ", UserId: " + userId);
                    
                    try {
                        UserEntity user = userRepository.findById(Long.parseLong(userId)).orElse(null);
                        if (user != null) {
                            // Desbloquear acceso a la verificación específica
                            org.cinos.core.posts.entity.PostEntity post = postRepository.findById(Long.parseLong(postId)).orElse(null);
                            if (post != null) {
                                if (!user.getUnlockedTechnicalVerifications().contains(post)) {
                                    user.getUnlockedTechnicalVerifications().add(post);
                                    userRepository.save(user);
                                    System.out.println("🔓 Acceso a verificación desbloqueado para usuario: " + user.getEmail() + " y post: " + postId);
                                } else {
                                    System.out.println("ℹ️ Usuario ya tenía acceso a esta verificación: " + user.getEmail() + " y post: " + postId);
                                }
                            } else {
                                System.err.println("❌ Post no encontrado con ID: " + postId);
                            }
                        } else {
                            System.err.println("❌ Usuario no encontrado con ID: " + userId);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error al procesar acceso a verificación: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ℹ️ Tipo de pago no manejado: " + type);
                }
            } else {
                System.err.println("❌ PaymentIntent es null incluso tras deserialización manual");
            }
        } else if ("customer.subscription.deleted".equals(eventType)) {
            System.out.println("➡️ Evento: customer.subscription.deleted");
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            String subscriptionId = root.get("id").asText();
            System.out.println("📦 subscriptionId desde rawJson: " + subscriptionId);
            var userOpt = userRepository.findByStripeSubscriptionId(subscriptionId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                if (user.getRoles() != null) {
                    user.getRoles().remove(org.cinos.core.users.model.Role.PREMIUM);
                }
                user.setStripeSubscriptionId(null);
                userRepository.save(user);
                System.out.println("🚨 Rol PREMIUM removido y subscriptionId limpiado para usuario: " + user.getEmail());
            } else {
                System.err.println("❌ Usuario no encontrado con subscriptionId: " + subscriptionId);
            }
        } else if ("customer.subscription.updated".equals(eventType)) {
            System.out.println("➡️ Evento: customer.subscription.updated");
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            String subscriptionId = root.get("id").asText();
            String status = root.get("status").asText();
            System.out.println("📦 subscriptionId: " + subscriptionId + ", status: " + status);
            
            var userOpt = userRepository.findByStripeSubscriptionId(subscriptionId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                
                // Si la suscripción se canceló pero aún está activa hasta el final del período
                if ("canceled".equals(status) || "unpaid".equals(status)) {
                    System.out.println("⚠️ Suscripción cancelada pero aún activa hasta el final del período para usuario: " + user.getEmail());
                    // El rol premium se mantiene hasta que la suscripción realmente termine
                } else if ("active".equals(status) || "trialing".equals(status)) {
                    System.out.println("✅ Suscripción activa para usuario: " + user.getEmail());
                    // Asegurar que el usuario tenga rol premium
                    if (user.getRoles() == null) {
                        user.setRoles(new ArrayList<>());
                    }
                    if (!user.getRoles().contains(Role.PREMIUM)) {
                        user.getRoles().add(Role.PREMIUM);
                        userRepository.save(user);
                        System.out.println("🚀 Rol PREMIUM agregado para usuario: " + user.getEmail());
                    }
                }
            } else {
                System.err.println("❌ Usuario no encontrado con subscriptionId: " + subscriptionId);
            }
        }

        else {
            System.out.println("ℹ️ Evento no manejado: " + eventType);
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }

    /**
     * Compra acceso a un informe técnico específico
     */
    @PostMapping("/buy-verification-access")
    public ResponseEntity<Map<String, String>> buyVerificationAccess(
            @RequestBody BuyVerificationAccessRequest request,
            @AuthenticationPrincipal UserEntity user) {
        try {
            String clientSecret = stripeService.createVerificationAccessPaymentIntent(request.postId(), user);
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
