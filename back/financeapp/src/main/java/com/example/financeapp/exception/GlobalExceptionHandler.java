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

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClientNotFound(ClientNotFoundException ex) {
        log.warn("Client not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("CLIENT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AkropoliIntegrationException.class)
    public ResponseEntity<Map<String, Object>> handleAkropoliIntegration(AkropoliIntegrationException ex) {
        log.error("Akropoli integration error [{}]: {}",
                ex.getErrorCode(), ex.getMessage(), ex);

        HttpStatus status = "AKROPOLI_AUTH_FAILED".equals(ex.getErrorCode())
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(status).body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private Map<String, Object> errorBody(String code, String message) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("errorCode", code);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}