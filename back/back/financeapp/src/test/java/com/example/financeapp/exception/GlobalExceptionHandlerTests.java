package com.example.financeapp.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapAuthenticationFailuresToBadGatewayWithoutApiDetail() {
        AkropoliIntegrationException exception =
                AkropoliIntegrationException.authenticationFailed(new RuntimeException("sensitive response"));

        ResponseEntity<Map<String, Object>> response = handler.handleAkropoliIntegration(exception);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("AKROPOLI_AUTH_FAILED", response.getBody().get("errorCode"));
        assertFalse(response.getBody().containsKey("apiDetail"));
    }

    @Test
    void shouldMapFetchFailuresToServiceUnavailableWithoutApiDetail() {
        AkropoliIntegrationException exception =
                AkropoliIntegrationException.fetchFailed("/v1/accounts", new RuntimeException("raw payload"));

        ResponseEntity<Map<String, Object>> response = handler.handleAkropoliIntegration(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("AKROPOLI_FETCH_FAILED", response.getBody().get("errorCode"));
        assertFalse(response.getBody().containsKey("apiDetail"));
    }
}