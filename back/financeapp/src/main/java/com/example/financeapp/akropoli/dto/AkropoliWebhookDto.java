package com.example.financeapp.akropoli.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload enviado pela Akropoli nos webhooks de status de consentimento.
 *
 * ATENÇÃO: o contrato exato de webhook da Akropoli (eventos, campos,
 * header de assinatura) deve ser confirmado com o suporte antes de ir
 * a produção. Os campos abaixo são o mínimo esperado com base na proposta
 * comercial e no padrão Open Finance Brasil.
 *
 * Eventos conhecidos do Open Finance Brasil:
 *   CONSENT_AUTHORISED     → consentimento concedido pelo usuário; link_id disponível
 *   CONSENT_REJECTED       → usuário recusou o consentimento
 *   CONSENT_REVOKED        → usuário ou instituição revogou o consentimento
 *   CONSENT_EXPIRED        → consentimento atingiu a data de expiração (max 12 meses BACEN)
 *   DATA_AVAILABLE         → novos dados disponíveis para o link_id
 *
 * Exemplo de body esperado:
 * {
 *   "event": "CONSENT_AUTHORISED",
 *   "link_id": "abc123def456",
 *   "consent_id": "urn:bancoex:C1DD33123",
 *   "client_user_id": "mongo-client-id-aqui",
 *   "timestamp": "2026-03-18T14:30:00Z"
 * }
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AkropoliWebhookDto {

    /**
     * Tipo do evento. Valores possíveis:
     * CONSENT_AUTHORISED, CONSENT_REJECTED, CONSENT_REVOKED,
     * CONSENT_EXPIRED, DATA_AVAILABLE
     */
    @JsonProperty("event")
    private String event;

    /**
     * ID do link (consentimento ativo) na Akropoli.
     * Equivalente ao pluggyItemId — chave para buscar dados do cliente.
     * Presente em todos os eventos exceto CONSENT_REJECTED.
     */
    @JsonProperty("link_id")
    private String linkId;

    /**
     * ID do consentimento no padrão Open Finance Brasil.
     * Formato: urn:{ispb}:{identificador}
     */
    @JsonProperty("consent_id")
    private String consentId;

    /**
     * ID do cliente no seu sistema (mongoClientId).
     * A Akropoli devolve o valor que você enviou na geração do link.
     */
    @JsonProperty("client_user_id")
    private String clientUserId;

    /** Timestamp ISO 8601 do evento. */
    @JsonProperty("timestamp")
    private String timestamp;

    /** URL do webhook cadastrada (informativo). */
    @JsonProperty("webhook_url")
    private String webhookUrl;
}