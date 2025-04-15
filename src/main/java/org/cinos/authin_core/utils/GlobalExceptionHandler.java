package org.cinos.authin_core.utils;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageUtil messageUtil;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerGenericException(HttpServletRequest req, Exception e) {
        String message = messageUtil.getMessage("error.generic", Locale.forLanguageTag("es"));
        ApiError apiError = ApiError.builder()
                .backendMessage(e.getLocalizedMessage())
                .message(message)
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now())
                .method(req.getMethod())
                .build();

        log.error("{}: {}", message, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handlerAccessDeniedException(HttpServletRequest req, AccessDeniedException e) {
        String message = messageUtil.getMessage("error.access.denied", Locale.forLanguageTag("es"));
        ApiError apiError = ApiError.builder()
                .backendMessage(e.getMessage())
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now())
                .method(req.getMethod())
                .message(message)
                .build();

        log.error("{}: {}", message, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handlerAccessDeniedException(HttpServletRequest req, BadCredentialsException e) {
        String message = messageUtil.getMessage("error.invalid.credentials", Locale.forLanguageTag("es"));
        ApiError apiError = ApiError.builder()
                .backendMessage(e.getMessage())
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now())
                .method(req.getMethod())
                .message(message)
                .build();

        log.error("{}: {}", message, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handlerExpiredJwtException(HttpServletRequest req, ExpiredJwtException e) {
        String message = messageUtil.getMessage("error.expired.jwt", Locale.forLanguageTag("es"));
        ApiError apiError = ApiError.builder()
                .backendMessage(e.getMessage())
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now())
                .method(req.getMethod())
                .message(message)
                .build();

        log.error("{}: {}", message, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(HttpServletRequest req, MethodArgumentNotValidException ex) {
        // Procesar errores
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Error no especificado"
                ));

        // Construir mensaje principal
        String errorSummary = errors.entrySet().stream()
                .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));

        // Construir respuesta
        ApiError apiError = ApiError.builder()
                .backendMessage("Validation error") // Más corto y genérico
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now()) // Formato consistente
                .method(req.getMethod())
                .message("Error de validación en los datos enviados: " + errorSummary)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }




}
