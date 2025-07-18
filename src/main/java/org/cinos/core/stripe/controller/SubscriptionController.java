package org.cinos.core.stripe.controller;

import com.stripe.model.Event;
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
import java.util.List;

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
            // Aquí deberías obtener el usuario actual del contexto de seguridad
            // Por ahora retornamos un estado simulado
            return ResponseEntity.ok(SubscriptionResponse.builder().message("active").success(true).build());
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
            return ResponseEntity.ok(SubscriptionResponse.builder().checkoutUrl(url).success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Webhook de Stripe para eventos de suscripción
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        String payload = "";
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload += line;
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("");
        }
        String sigHeader = request.getHeader("Stripe-Signature");
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("");
        }
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String email = session.getCustomerDetails().getEmail();
                UserEntity user = userRepository.findByEmail(email).orElse(null);
                if (user != null && (user.getRoles() == null || !user.getRoles().contains(Role.PREMIUM))) {
                    if (user.getRoles() == null) user.setRoles(new java.util.ArrayList<>());
                    user.getRoles().add(Role.PREMIUM);
                    userRepository.save(user);
                }
            }
        }
        return ResponseEntity.ok("");
    }
}
