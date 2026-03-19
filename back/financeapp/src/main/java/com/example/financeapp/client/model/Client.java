package com.example.financeapp.client.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    private String id;

    private String name;

    // ── Campos criptografados (AES-256-GCM via EncryptionMongoEventListener) ──

    /** Email em ciphertext — nunca comparar diretamente em queries. */
    private String email;

    /** CPF em ciphertext — nunca comparar diretamente em queries. */
    private String cpf;

    // ── Campos de hash para busca (HMAC-SHA256 via DeterministicHashService) ──

    /**
     * HMAC-SHA256 do email (lowercase) — usado em findByEmailHash().
     * Determinístico: o mesmo email sempre produz o mesmo hash.
     * Indexado com unique=true para garantir unicidade mesmo com email criptografado.
     */
    @Indexed(unique = true)
    private String emailHash;

    /**
     * HMAC-SHA256 do CPF — usado em findByCpfHash().
     * Determinístico: o mesmo CPF sempre produz o mesmo hash.
     * Indexado com unique=true para impedir duplicatas mesmo com CPF criptografado.
     */
    @Indexed(unique = true)
    private String cpfHash;

    // ── Campos normais (não criptografados) ───────────────────────────────────

    private String phone;

    @Indexed
    private String bankerId;

    /**
     * Link ID retornado pela Akropoli após o usuário completar a jornada
     * de consentimento Open Finance. Criptografado via EncryptionMongoEventListener.
     */
    @Indexed
    private String akropoliLinkId;

    /**
     * @deprecated Use akropoliLinkId. Mantido apenas para compatibilidade
     * durante a migração de dados existentes.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    private String pluggyItemId;

    @Builder.Default
    private ConnectionStatus connectionStatus = ConnectionStatus.PENDING;

    /**
     * Timestamp de expiração do consentimento Open Finance (máx. 12 meses BACEN).
     */
    private LocalDateTime consentExpiresAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime lastSync;

    public enum ConnectionStatus {
        PENDING,
        AWAITING_CONSENT,
        UPDATING,
        UPDATED,
        LOGIN_ERROR,
        CONNECTION_ERROR,
        CONSENT_EXPIRED,
        CONSENT_REVOKED
    }
}