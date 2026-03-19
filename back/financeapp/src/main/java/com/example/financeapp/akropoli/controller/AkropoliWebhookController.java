package com.example.financeapp.akropoli.controller;

import com.example.financeapp.akropoli.config.AkropoliProperties;
import com.example.financeapp.akropoli.dto.AkropoliWebhookDto;
import com.example.financeapp.akropoli.service.AkropoliService;
import com.example.financeapp.exception.ClientNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class AkropoliWebhookController {

    private static final String SIGNATURE_HEADER = "akropoli-signature";
    private static final String HMAC_ALGORITHM   = "HmacSHA256";

    private final AkropoliService akropoliService;
    private final AkropoliProperties properties;
    private final ObjectMapper objectMapper;

    @PostMapping("/akropoli")
    public ResponseEntity<Void> handleAkropoliWebhook(
            @RequestHeader(value = SIGNATURE_HEADER, required = false) String signature,
            @RequestBody String rawBody) {

        if (!isSignatureValid(rawBody, signature)) {
            log.warn("Akropoli webhook rejected: invalid or missing signature");
            // Erro permanente: 401 Unauthorized (Akropoli não deve tentar de novo com a mesma assinatura)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AkropoliWebhookDto payload;
        try {
            payload = objectMapper.readValue(rawBody, AkropoliWebhookDto.class);
        } catch (Exception e) {
            log.error("Akropoli webhook rejected: failed to parse payload");
            // Erro permanente: 400 Bad Request (Não faz sentido tentar ler de novo um payload mal formado)
            return ResponseEntity.badRequest().build();
        }

        // Mascarar o linkId nos logs para evitar vazamento de PII!
        String maskedLinkId = payload.getLinkId() != null
                ? payload.getLinkId().substring(Math.max(0, payload.getLinkId().length() - 4))
                : "null";

        log.info("Akropoli webhook event={} linkId=***{}", payload.getEvent(), maskedLinkId);

        try {
            processEvent(payload);
            return ResponseEntity.ok().build(); // Sucesso

        } catch (ClientNotFoundException | IllegalStateException e) {
            // Erros de Negócio: Cliente não existe ou estado inválido.
            // Retornamos 200 OK para fazer "Ack" (Acknowledge) e pedir à Akropoli para parar de tentar.
            log.warn("Business error processing webhook, discarding event. Reason: {}", e.getMessage());
            return ResponseEntity.ok().build();

        } catch (DataAccessException e) {
            // Erro de Infraestrutura: Base de dados em baixo, timeout, etc.
            // Retornamos 503 para a Akropoli aplicar o "Retry Policy" e tentar mais tarde.
            log.error("Database unavailable during webhook processing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        } catch (Exception e) {
            // Outros erros genéricos: Retornamos 500 para permitir retentativas.
            log.error("Unexpected error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void processEvent(AkropoliWebhookDto payload) {
        switch (payload.getEvent()) {
            case "CONSENT_AUTHORISED" -> {
                if (payload.getLinkId() != null && payload.getClientUserId() != null) {
                    akropoliService.handleConsentAuthorised(payload.getLinkId(), payload.getClientUserId());
                }
            }
            case "DATA_AVAILABLE" -> {
                if (payload.getLinkId() != null) akropoliService.syncByLinkId(payload.getLinkId());
            }
            case "CONSENT_EXPIRED" -> akropoliService.markConsentExpired(payload.getLinkId());
            case "CONSENT_REVOKED" -> akropoliService.markConsentRevoked(payload.getLinkId());
            case "CONSENT_REJECTED" -> log.info("Consent rejected by user clientUserId={}", payload.getClientUserId());
            default -> log.info("Akropoli webhook event '{}' not handled", payload.getEvent());
        }
    }

    @Profile("dev")
    @PostMapping("/test/akropoli")
    public ResponseEntity<Void> handleAkropoliWebhookTest(
            @RequestBody(required = false) String payload) {
        log.info("Test webhook received: {}", payload);
        return ResponseEntity.ok().build();
    }

    private boolean isSignatureValid(String body, String receivedSignature) {
        String secret = properties.getWebhookSecret();

        if (secret == null || secret.isBlank()) {
            log.debug("akropoli.webhook-secret not configured — skipping HMAC (dev mode)");
            return true;
        }

        if (receivedSignature == null || receivedSignature.isBlank()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            String computedHex = HexFormat.of()
                    .formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));

            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    receivedSignature.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("HMAC verification error: {}", e.getMessage());
            return false;
        }
    }
}