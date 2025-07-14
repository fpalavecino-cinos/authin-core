package org.cinos.core.stripe.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.cinos.core.stripe.dto.SubscriptionPlanDto;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    private final UserRepository userRepository;

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
        Optional<UserEntity> userOpt = userRepository.findById(Long.valueOf(userId));
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (user.getStripeCustomerId() != null) {
                return user.getStripeCustomerId();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        Customer customer = Customer.create(params);
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
        return customer.getId();
        } else {
            throw new RuntimeException("Usuario no encontrado para crear customer en Stripe");
        }
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
        PaymentIntent paymentIntent = invoice != null ? invoice.getPaymentIntentObject() : null;
        if (paymentIntent != null) {
        return paymentIntent.getClientSecret();
        } else {
            // No hay PaymentIntent (no hay cobro inmediato), forzar recolección de tarjeta con SetupIntent
            return createSetupIntent(userId, email);
        }
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
     * Crea un SetupIntent para forzar la recolección de tarjeta aunque sea trial
     */
    public String createSetupIntent(String userId, String email) throws StripeException {
        String customerId = getOrCreateCustomer(userId, email);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        SetupIntent setupIntent = SetupIntent.create(params);
        return setupIntent.getClientSecret();
    }
}