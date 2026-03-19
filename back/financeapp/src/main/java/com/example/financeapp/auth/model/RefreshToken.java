package com.example.financeapp.auth.model;

import com.example.financeapp.config.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_hash",   columnList = "token_hash", unique = true),
                @Index(name = "idx_refresh_token_banker", columnList = "banker_id"),
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "banker_id", nullable = false)
    private String bankerId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Preenchido via @Builder.Default — nunca depende de @EnableJpaAuditing.
     * Sem este default, o campo é null quando usamos .builder()...build()
     * e o Postgres rejeita com NOT NULL violation.
     */
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── helpers ───────────────────────────────────────────────────────────

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }
}