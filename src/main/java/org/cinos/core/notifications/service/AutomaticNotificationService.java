package org.cinos.core.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cinos.core.notifications.dto.PushNotificationRequest;
import org.cinos.core.notifications.entity.PushTokenEntity;
import org.cinos.core.notifications.repository.PushTokenRepository;
import org.cinos.core.posts.entity.PostEntity;
import org.cinos.core.users.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomaticNotificationService {

    private final PushNotificationService pushNotificationService;
    private final PushTokenRepository pushTokenRepository;

    /**
     * Notifica a usuarios premium sobre un nuevo post
     */
    public void notifyNewPost(PostEntity post) {
        try {
            String title = "🚗 Nuevo vehículo disponible";
            String body = String.format("Se ha publicado un nuevo %s %s", 
                    post.getMake(), post.getModel());

            Map<String, String> data = Map.of(
                    "type", "NEW_POST",
                    "postId", post.getId().toString(),
                    "make", post.getMake(),
                    "model", post.getModel()
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Notificación de nuevo post enviada a usuarios premium");
        } catch (Exception e) {
            log.error("Error enviando notificación de nuevo post: {}", e.getMessage());
        }
    }

    /**
     * Notifica cuando se completa una verificación técnica
     */
    public void notifyVerificationCompleted(UserEntity user, String make, String model) {
        try {
            String title = "✅ Verificación técnica completada";
            String body = String.format("La verificación técnica del %s %s ha sido completada", make, model);

            Map<String, String> data = Map.of(
                    "type", "VERIFICATION_COMPLETED",
                    "make", make,
                    "model", model
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .userIds(java.util.List.of(user.getId()))
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Notificación de verificación completada enviada al usuario: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error enviando notificación de verificación: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre actualizaciones de suscripción
     */
    public void notifySubscriptionUpdate(UserEntity user, String message) {
        try {
            String title = "💎 Actualización de suscripción";
            String body = message;

            Map<String, String> data = Map.of(
                    "type", "SUBSCRIPTION_UPDATE",
                    "message", message
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .userIds(java.util.List.of(user.getId()))
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Notificación de suscripción enviada al usuario: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error enviando notificación de suscripción: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre nuevas características premium
     */
    public void notifyPremiumFeature(String featureName, String description) {
        try {
            String title = "🌟 Nueva característica premium";
            String body = String.format("%s: %s", featureName, description);

            Map<String, String> data = Map.of(
                    "type", "PREMIUM_FEATURE",
                    "feature", featureName,
                    "description", description
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Notificación de nueva característica premium enviada");
        } catch (Exception e) {
            log.error("Error enviando notificación de característica premium: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre anuncios del sistema
     */
    public void notifySystemAnnouncement(String title, String message) {
        try {
            Map<String, String> data = Map.of(
                    "type", "SYSTEM_ANNOUNCEMENT",
                    "message", message
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(message)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Anuncio del sistema enviado a usuarios premium");
        } catch (Exception e) {
            log.error("Error enviando anuncio del sistema: {}", e.getMessage());
        }
    }

    /**
     * Notifica sobre ofertas especiales para usuarios premium
     */
    public void notifyPremiumOffer(String offerTitle, String offerDescription) {
        try {
            String title = "🎁 Oferta especial premium";
            String body = String.format("%s: %s", offerTitle, offerDescription);

            Map<String, String> data = Map.of(
                    "type", "PREMIUM_OFFER",
                    "offerTitle", offerTitle,
                    "offerDescription", offerDescription
            );

            PushNotificationRequest request = PushNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .data(data)
                    .sendToPremiumOnly(true)
                    .build();

            pushNotificationService.sendNotificationToUsers(request);
            log.info("Oferta premium enviada a usuarios premium");
        } catch (Exception e) {
            log.error("Error enviando oferta premium: {}", e.getMessage());
        }
    }
} 