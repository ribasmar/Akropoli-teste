package com.example.financeapp.akropoli.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração da integração Akropoli Open Finance.
 *
 * Segurança:
 *   @ToString.Exclude em clientSecret e webhookSecret garante que esses
 *   valores nunca apareçam em logs, mesmo via Spring Actuator ou stack traces.
 *
 * application.yml:
 *   akropoli:
 *     base-url: ${AKROPOLI_BASE_URL}
 *     client-id: ${AKROPOLI_CLIENT_ID}
 *     client-secret: ${AKROPOLI_CLIENT_SECRET}
 *     webhook-secret: ${AKROPOLI_WEBHOOK_SECRET:}
 *     token-ttl-seconds: ${AKROPOLI_TOKEN_TTL:3600}
 *
 * Nota sobre token TTL:
 *   A Akropoli não documenta publicamente o TTL do token de acesso.
 *   O valor padrão de 3600s (1h) é conservador — confirmar com o suporte
 *   e ajustar via variável de ambiente se necessário.
 */
@Data
@Component
@ConfigurationProperties(prefix = "akropoli")
public class AkropoliProperties {

    private String baseUrl;
    private String clientId;

    /**
     * Client secret da aplicação Akropoli.
     * Excluído do toString() para não vazar em logs.
     */
    @ToString.Exclude
    private String clientSecret;

    /**
     * Secret HMAC para verificação de assinatura dos webhooks recebidos.
     * Excluído do toString() para não vazar em logs.
     * Deixar em branco desativa a verificação HMAC (útil apenas em dev).
     */
    @ToString.Exclude
    private String webhookSecret;

    /**
     * TTL do token de acesso em segundos.
     * O cache renova com 60s de margem antes do vencimento.
     * Default: 3600s (1 hora).
     */
    private long tokenTtlSeconds = 3600L;
}