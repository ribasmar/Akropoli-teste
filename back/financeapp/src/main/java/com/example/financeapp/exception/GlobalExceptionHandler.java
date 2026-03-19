package com.example.financeapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tratamento centralizado de exceções da aplicação.

 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── ClientNotFoundException ───────────────────────────────────────────────

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClientNotFound(ClientNotFoundException ex) {
        log.warn("Client not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("CLIENT_NOT_FOUND", ex.getMessage()));
    }

    // ── AkropoliIntegrationException ──────────────────────────────────────────

    @ExceptionHandler(AkropoliIntegrationException.class)
    public ResponseEntity<Map<String, Object>> handleAkropoliIntegration(AkropoliIntegrationException ex) {
        // Loga apenas errorCode e message — nunca o pluggyResponse (pode conter PII/dados bancários)
        log.error("Akropoli integration error [{}]: {}", ex.getErrorCode(), ex.getMessage());

        // Erros de autenticação/escopo → 502 Bad Gateway (problema com a API externa)
        // Demais erros de integração → 503 Service Unavailable
        HttpStatus status = "AKROPOLI_AUTH_FAILED".equals(ex.getErrorCode())
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.SERVICE_UNAVAILABLE;

        // Nunca devolver apiDetail ao cliente — pode conter dados sensíveis da Akropoli
        return ResponseEntity.status(status).body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    // ── IllegalStateException ─────────────────────────────────────────────────

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("CONFLICT", ex.getMessage()));
    }

    // ── Fallback ──────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> errorBody(String code, String message) {
        // Using java.util.HashMap directly to keep the map mutable
        // (so callers can add extra fields like "apiDetail")
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("errorCode", code);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}