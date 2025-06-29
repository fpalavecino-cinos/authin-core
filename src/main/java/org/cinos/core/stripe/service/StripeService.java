package org.cinos.core.stripe.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import org.cinos.core.stripe.dto.SubscriptionPlanDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Obtiene los planes de suscripción disponibles
     */
    public List<SubscriptionPlanDto> getSubscriptionPlans() {
        List<SubscriptionPlanDto> plans = new ArrayList<>();

        // Plan Premium Mensual
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

        // Plan Premium Anual
        plans.add(new SubscriptionPlanDto(
                "premium_yearly",
                "Premium Anual",
                9999, // $99.99 en centavos
                "USD",
                "year",
                List.of(
                        "Todo lo del plan mensual",
                        "2 meses gratis",
                        "Descuento del 17%",
                        "Acceso anticipado a nuevas funciones"
                )
        ));

        return plans;
    }

    /**
     * Crea una suscripción y retorna el client secret
     * Versión simplificada usando solo PaymentIntent
     */
    public String createSubscription(String planId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", getAmountForPlan(planId));
        params.put("currency", "usd");

        // Configurar métodos de pago automáticos
        Map<String, Object> automaticPaymentMethods = new HashMap<>();
        automaticPaymentMethods.put("enabled", true);
        params.put("automatic_payment_methods", automaticPaymentMethods);

        // Agregar metadata para identificar el plan
        Map<String, String> metadata = new HashMap<>();
        metadata.put("planId", planId);
        metadata.put("type", "subscription");
        params.put("metadata", metadata);

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return paymentIntent.getClientSecret();
    }

    /**
     * Cancela una suscripción o pago
     */
    public void cancelSubscription(String subscriptionId) throws Exception {
        try {
            // Intentar cancelar como suscripción
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.cancel();
        } catch (Exception e) {
            try {
                // Si no es una suscripción, intentar cancelar como PaymentIntent
                PaymentIntent paymentIntent = PaymentIntent.retrieve(subscriptionId);
                paymentIntent.cancel();
            } catch (Exception ex) {
                throw new Exception("No se pudo cancelar la suscripción o pago");
            }
        }
    }

    /**
     * Obtiene el Price ID de Stripe para un plan
     */
    private String getPriceIdForPlan(String planId) {
        Map<String, String> planToPriceId = new HashMap<>();
        planToPriceId.put("premium_monthly", "price_premium_monthly"); // Reemplaza con tu Price ID real
        planToPriceId.put("premium_yearly", "price_premium_yearly");   // Reemplaza con tu Price ID real

        return planToPriceId.getOrDefault(planId, "price_premium_monthly");
    }

    /**
     * Obtiene el monto en centavos para un plan
     */
    private Long getAmountForPlan(String planId) {
        Map<String, Long> planToAmount = new HashMap<>();
        planToAmount.put("premium_monthly", 999L);  // $9.99
        planToAmount.put("premium_yearly", 9999L);  // $99.99

        return planToAmount.getOrDefault(planId, 999L);
    }

    /**
     * Verifica el estado de un pago
     */
    public String checkPaymentStatus(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.getStatus();
    }

    /**
     * Confirma un pago exitoso y actualiza el usuario
     */
    public boolean confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        if ("succeeded".equals(paymentIntent.getStatus())) {
            // Aquí deberías actualizar el usuario en tu base de datos
            // Por ejemplo, agregar el rol PREMIUM
            return true;
        }

        return false;
    }

    /**
     * Crea un pago único (alternativa a suscripción recurrente)
     */
    public String createOneTimePayment(String planId, String customerId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", getAmountForPlan(planId));
        params.put("currency", "usd");

        if (customerId != null && !customerId.isEmpty()) {
            params.put("customer", customerId);
        }

        // Configurar métodos de pago automáticos
        Map<String, Object> automaticPaymentMethods = new HashMap<>();
        automaticPaymentMethods.put("enabled", true);
        params.put("automatic_payment_methods", automaticPaymentMethods);

        // Agregar metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("planId", planId);
        metadata.put("type", "one_time_payment");
        params.put("metadata", metadata);

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return paymentIntent.getClientSecret();
    }
}