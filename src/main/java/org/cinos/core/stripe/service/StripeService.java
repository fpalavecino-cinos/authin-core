package org.cinos.core.stripe.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.cinos.core.stripe.dto.SubscriptionPlanDto;
import org.cinos.core.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // Simulación de base de datos para ejemplo (reemplaza por tu repo real)
    private final Map<String, String> userIdToCustomerId = new HashMap<>();
    private final Map<String, String> userIdToSubscriptionId = new HashMap<>();

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Devuelve solo el plan mensual
     */
    public List<SubscriptionPlanDto> getSubscriptionPlans() {
        List<SubscriptionPlanDto> plans = new ArrayList<>();
        plans.add(new SubscriptionPlanDto(
                "premium_monthly",
                "Premium Mensual",
                999, // $9.99 en centavos
                "USD",
                "month",
                List.of(
                        "Filtros avanzados ilimitados",
                        "Recomendaciones ilimitadas",
                        "Sin anuncios",
                        "Soporte prioritario"
                )
        ));
        return plans;
    }

    /**
     * Obtiene el Price ID real de Stripe para el plan mensual
     */
    private String getPriceIdForPlan(String planId) {
        Map<String, String> planToPriceId = new HashMap<>();
        planToPriceId.put("premium_monthly", "price_1RfkUKCTvKLO8QJ3rRZotpQl"); // Price ID real
        return planToPriceId.getOrDefault(planId, "price_1RfkUKCTvKLO8QJ3rRZotpQl");
    }

    /**
     * Obtiene o crea un Customer de Stripe para el usuario
     */
    private String getOrCreateCustomer(String userId, String email) throws StripeException {
        // Busca en tu base de datos real
        if (userIdToCustomerId.containsKey(userId)) {
            return userIdToCustomerId.get(userId);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        Customer customer = Customer.create(params);
        userIdToCustomerId.put(userId, customer.getId());
        return customer.getId();
    }

    /**
     * Crea una suscripción real en Stripe y retorna el clientSecret del PaymentIntent de la primera factura
     */
    public String createSubscription(String planId, String userId, String email, boolean trial) throws StripeException {
        String customerId = getOrCreateCustomer(userId, email);

        Map<String, Object> item = new HashMap<>();
        item.put("price", getPriceIdForPlan(planId));
        List<Object> items = new ArrayList<>();
        items.add(item);

        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("items", items);

        if (trial) {
            params.put("trial_period_days", 7); // Cambia el periodo si lo deseas
        }

        params.put("payment_behavior", "default_incomplete");
        params.put("expand", List.of("latest_invoice.payment_intent"));

        Subscription subscription = Subscription.create(params);

        userIdToSubscriptionId.put(userId, subscription.getId());

        Invoice invoice = subscription.getLatestInvoiceObject();
        PaymentIntent paymentIntent = invoice.getPaymentIntentObject();
        return paymentIntent.getClientSecret();
    }

    /**
     * Cancela una suscripción activa
     */
    public void cancelSubscription(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        subscription.cancel();
        // Actualiza tu base de datos para marcar al usuario como no premium
    }

    /**
     * Verifica el estado de la suscripción de un usuario
     */
    public String getSubscriptionStatus(String userId) throws StripeException {
        String subscriptionId = userIdToSubscriptionId.get(userId);
        if (subscriptionId == null) return "none";
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.getStatus(); // Ej: active, canceled, incomplete, etc.
    }

    /**
     * Verifica el estado de un pago (opcional)
     */
    public String checkPaymentStatus(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.getStatus();
    }

    /**
     * Confirma un pago exitoso (opcional, lo ideal es usar webhooks)
     */
    public boolean confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return "succeeded".equals(paymentIntent.getStatus());
    }

    /**
     * Crea una sesión de Stripe Checkout para suscripción
     */
    public String createSubscriptionCheckoutSession(String priceId, String successUrl, String cancelUrl, String customerEmail) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setCustomerEmail(customerEmail)
                .build();
        Session session = Session.create(params);
        return session.getUrl();
    }

    /**
     * Obtiene la fecha de próxima renovación de una suscripción de Stripe
     */
    public Long getSubscriptionNextRenewal(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.getCurrentPeriodEnd(); // Epoch seconds
    }

    public String getLastCreatedSubscriptionIdForUser(String userId) {
        return userIdToSubscriptionId.get(userId);
    }

    public static class StripeSubscriptionResult {
        public final String clientSecret;
        public final String subscriptionId;
        public StripeSubscriptionResult(String clientSecret, String subscriptionId) {
            this.clientSecret = clientSecret;
            this.subscriptionId = subscriptionId;
        }
    }

    public StripeSubscriptionResult createSubscriptionWithId(String planId, String userId, String email, boolean trial) throws StripeException {
        String customerId = getOrCreateCustomer(userId, email);
        Map<String, Object> item = new HashMap<>();
        item.put("price", getPriceIdForPlan(planId));
        List<Object> items = new ArrayList<>();
        items.add(item);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("items", items);
        if (trial) {
            params.put("trial_period_days", 7);
        }
        params.put("payment_behavior", "default_incomplete");
        params.put("expand", List.of("latest_invoice.payment_intent"));
        Subscription subscription = Subscription.create(params);
        Invoice invoice = subscription.getLatestInvoiceObject();
        PaymentIntent paymentIntent = invoice.getPaymentIntentObject();
        return new StripeSubscriptionResult(paymentIntent.getClientSecret(), subscription.getId());
    }

    public String createVerificationAccessPaymentIntent(Long postId, UserEntity user) throws StripeException {
        // Crear PaymentIntent para acceso a verificación específica
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(499L) // $4.99 en centavos
            .setCurrency("usd")
            .setCustomer(user.getStripeCustomerId())
            .putMetadata("postId", postId.toString())
            .putMetadata("userId", user.getId().toString())
            .putMetadata("type", "verification_access")
            .setDescription("Acceso a informe técnico")
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                    .build()
            )
            .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        System.out.println("🔧 PaymentIntent creado con ID: " + paymentIntent.getId() + " y estado: " + paymentIntent.getStatus());
        
        // Confirmar el PaymentIntent inmediatamente con un método de pago de prueba
        Map<String, Object> confirmParams = new HashMap<>();
        confirmParams.put("payment_method_data", Map.of(
            "type", "card",
            "card", Map.of(
                "number", "4242424242424242",
                "exp_month", 12,
                "exp_year", 2024,
                "cvc", "123"
            )
        ));
        
        paymentIntent = paymentIntent.confirm(confirmParams);
        System.out.println("✅ PaymentIntent confirmado con estado: " + paymentIntent.getStatus());
        
        return paymentIntent.getClientSecret();
    }
}