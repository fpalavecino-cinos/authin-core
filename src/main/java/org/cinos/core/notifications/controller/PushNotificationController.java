package org.cinos.core.notifications.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cinos.core.notifications.dto.PushNotificationRequest;
import org.cinos.core.notifications.dto.PushNotificationResponse;
import org.cinos.core.notifications.dto.TokenRegistrationRequest;
import org.cinos.core.notifications.entity.PushTokenEntity;
import org.cinos.core.notifications.service.PushNotificationService;
import org.cinos.core.users.entity.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    /**
     * Registra un token de notificación para el usuario autenticado
     */
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, Object>> registerToken(@RequestBody TokenRegistrationRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();

            pushNotificationService.registerToken(user, request.getToken(), request.getDeviceType());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token registrado exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error registrando token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error registrando token: " + e.getMessage()
            ));
        }
    }

    /**
     * Elimina un token de notificación
     */
    @DeleteMapping("/unregister-token/{token}")
    public ResponseEntity<Map<String, Object>> unregisterToken(@PathVariable String token) {
        try {
            pushNotificationService.unregisterToken(token);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token eliminado exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error eliminando token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error eliminando token: " + e.getMessage()
            ));
        }
    }

    /**
     * Envía notificación a usuarios específicos (solo para administradores)
     */
    @PostMapping("/send")
    public ResponseEntity<PushNotificationResponse> sendNotification(@RequestBody PushNotificationRequest request) {
        try {
            // Verificar si el usuario es administrador (implementar según tu lógica de roles)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            
            // Aquí deberías verificar si el usuario tiene permisos de administrador
            // Por ahora, permitimos a todos los usuarios premium enviar notificaciones
            
            PushNotificationResponse response = pushNotificationService.sendNotificationToUsers(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error enviando notificación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(PushNotificationResponse.builder()
                    .success(false)
                    .message("Error enviando notificación: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Envía notificación a todos los usuarios premium
     */
    @PostMapping("/send-to-premium")
    public ResponseEntity<PushNotificationResponse> sendToPremiumUsers(@RequestBody Map<String, String> notification) {
        try {
            String title = notification.get("title");
            String body = notification.get("body");
            Map<String, String> data = notification.containsKey("data") ? 
                    Map.of("data", notification.get("data")) : null;

            PushNotificationResponse response = pushNotificationService.sendNotificationToPremiumUsers(title, body, data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error enviando notificación a usuarios premium: {}", e.getMessage());
            return ResponseEntity.badRequest().body(PushNotificationResponse.builder()
                    .success(false)
                    .message("Error enviando notificación: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Envía notificación a un topic específico
     */
    @PostMapping("/send-to-topic/{topic}")
    public ResponseEntity<Map<String, Object>> sendToTopic(
            @PathVariable String topic,
            @RequestBody Map<String, String> notification) {
        try {
            String title = notification.get("title");
            String body = notification.get("body");
            Map<String, String> data = notification.containsKey("data") ? 
                    Map.of("data", notification.get("data")) : null;

            pushNotificationService.sendNotificationToTopic(topic, title, body, data);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notificación enviada al topic: " + topic
            ));
        } catch (Exception e) {
            log.error("Error enviando notificación al topic {}: {}", topic, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error enviando notificación: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtiene estadísticas de notificaciones (solo para administradores)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            // Aquí podrías implementar estadísticas de notificaciones
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Estadísticas obtenidas exitosamente",
                    "stats", Map.of(
                            "totalTokens", 0,
                            "premiumTokens", 0,
                            "activeTokens", 0
                    )
            ));
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error obteniendo estadísticas: " + e.getMessage()
            ));
        }
    }
} 