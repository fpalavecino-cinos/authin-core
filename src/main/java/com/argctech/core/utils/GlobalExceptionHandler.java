package com.argctech.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageUtil messageUtil;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerGenericException(HttpServletRequest req, Exception e) {
        ApiError apiError = ApiError.builder()
                .backendMessage(e.getLocalizedMessage())
                .message(messageUtil.getMessage("error.generic", Locale.forLanguageTag("es")))
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now())
                .method(req.getMethod())
                .build();

        log.error(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).reduce("", (a, b) -> a + "\n" + b));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handlerAccessDeniedException(HttpServletRequest req, AccessDeniedException e) {
        ApiError apiError = ApiError.builder()
                .backendMessage(e.getMessage())
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now())
                .method(req.getMethod())
                .message(messageUtil.getMessage("error.access.denied", Locale.forLanguageTag("es")))
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handlerAccessDeniedException(HttpServletRequest req, BadCredentialsException e) {
        ApiError apiError = ApiError.builder()
                .backendMessage(e.getMessage())
                .url(req.getRequestURL().toString())
                .date(LocalDateTime.now())
                .method(req.getMethod())
                .message(messageUtil.getMessage("error.invalid.credentials", Locale.forLanguageTag("es")))
                .build();

        log.error(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).reduce("", (a, b) -> a + "\n" + b));
        log.error(messageUtil.getMessage("invalid.credentials", Locale.getDefault()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }




}
