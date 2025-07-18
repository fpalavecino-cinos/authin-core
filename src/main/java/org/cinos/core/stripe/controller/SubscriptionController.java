package org.cinos.core.stripe.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.cinos.core.stripe.dto.CreateSubscriptionRequest;
import org.cinos.core.stripe.dto.SubscriptionPlanDto;
import org.cinos.core.stripe.dto.SubscriptionResponse;
import org.cinos.core.stripe.service.StripeService;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.model.Role;
import org.cinos.core.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import org.cinos.core.stripe.dto.StripeInvoicePayment;

@RestController
@RequestMapping("subscriptions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SubscriptionController {

    private final StripeService stripeService;

    @Autowired
    private UserRepository userRepository;

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

            String clientSecret = stripeService.createSubscription(
                request.getPlanId(),
                userId,
                email,
                request.isTrial()
            );
            return ResponseEntity.ok(SubscriptionResponse.builder().clientSecret(clientSecret).success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder()
                            .message("Error: " + e.getMessage())
                            .success(false)
                            .build());
        }
    }

    /**
     * Cancela una suscripción
     */
    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@RequestBody String subscriptionId) {
        try {
            if (subscriptionId == null || subscriptionId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Subscription ID es requerido")
                                .success(false)
                                .build());
            }

            stripeService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok(
                    SubscriptionResponse.builder()
                            .message("Suscripción cancelada exitosamente")
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
     * Webhook de Stripe para eventos de suscripción
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
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

            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String email = session.getCustomerDetails().getEmail();
                System.out.println("Email cliente: " + email);
                // No se actualiza rol aquí, solo logging
            } else {
                System.err.println("❌ Session es null");
            }

        } else if ("invoice.payment_succeeded".equals(eventType) || "invoice_payment.paid".equals(eventType)) {
            System.out.println("➡️ Evento: " + eventType);

            // Si el objeto no se puede deserializar automáticamente, lo parseamos manualmente
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            String invoiceId = null;
            try {
                if (deserializer.getObject().isPresent()) {
                    Object dataObject = deserializer.getObject().get();
                    if (dataObject instanceof com.stripe.model.Invoice) {
                        invoiceId = ((com.stripe.model.Invoice) dataObject).getId();
                    }
                } else {
                    // Si no se pudo deserializar, parseamos el JSON crudo
                    String rawJson = deserializer.getRawJson();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(rawJson);
                    invoiceId = root.get("id").asText();
                    System.out.println("📦 invoiceId desde rawJson: " + invoiceId);
                }
            } catch (Exception e) {
                System.err.println("❌ Error al extraer invoice ID: " + e.getMessage());
            }

            if (invoiceId != null) {
                try {
                    com.stripe.model.Invoice invoice = com.stripe.model.Invoice.retrieve(invoiceId);
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
                            user.getRoles().add(Role.PREMIUM);
                            userRepository.save(user);
                            System.out.println("🚀 Usuario actualizado a PREMIUM: " + user.getEmail());
                        } else {
                            System.out.println("✅ Usuario ya es PREMIUM");
                        }
                    } else {
                        System.err.println("❌ Usuario no encontrado con email: " + email);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al recuperar invoice/customer: " + e.getMessage());
                }
            } else {
                System.err.println("⚠️ No se pudo obtener invoice ID");
            }

        } else {
            System.out.println("ℹ️ Evento no manejado: " + eventType);
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }

}
