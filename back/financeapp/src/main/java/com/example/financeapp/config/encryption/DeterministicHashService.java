package com.example.financeapp.config.encryption;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Gera hashes determinísticos (HMAC-SHA256) para campos que precisam
 * ser buscados no banco mesmo estando criptografados com AES-256-GCM.
 *
 * CAMPOS COM HASH:
 *   Client (MongoDB):
 *     - emailHash   → busca por email criptografado
 *     - cpfHash     → busca por CPF criptografado + verificação de unicidade
 *
 *   Banker (MongoDB):
 *     - emailHash   → login e verificação de existência
 *
 * CHAVE SEPARADA DA CHAVE DE CRIPTOGRAFIA:
 *   Usa HASH_SECRET (variável de ambiente separada de ENCRYPTION_KEY).
 *   Separar as chaves garante que comprometer uma não compromete a outra.
 *   Gerar com: openssl rand -base64 32
 *
 * NORMALIZAÇÃO:
 *   Email é convertido para lowercase antes do hash para garantir que
 *   "Ana@Email.com" e "ana@email.com" produzam o mesmo hash.
 *   CPF é armazenado apenas como dígitos (sem formatação) — já garantido
 *   pela anotação @ValidCpf.
 */
@Slf4j
@Component
public class DeterministicHashService {

    private static final String ALGORITHM = "HmacSHA256";

    @Value("${encryption.hash-secret}")
    private String hashSecretBase64;

    private byte[] hashKey;

    @PostConstruct
    void init() {
        hashKey = java.util.Base64.getDecoder().decode(hashSecretBase64);
        if (hashKey.length < 32) {
            throw new IllegalStateException(
                    "HASH_SECRET must be at least 32 bytes encoded as Base64. " +
                            "Generate with: openssl rand -base64 32"
            );
        }
        log.info("Deterministic hash service initialized (HMAC-SHA256)");
    }

    /**
     * Gera o hash determinístico de um valor para armazenamento/busca.
     *
     * @param value valor em plaintext (ex: CPF "12345678901", email "ana@email.com")
     * @return HMAC-SHA256 em hex (64 chars) ou null se value for null
     */
    public String hash(String value) {
        if (value == null) return null;
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(hashKey, ALGORITHM));
            byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256", e);
        }
    }

    /**
     * Hash de email — normaliza para lowercase antes de hash.
     * Garante que "Ana@Email.com" e "ana@email.com" produzam o mesmo hash.
     */
    public String hashEmail(String email) {
        if (email == null) return null;
        return hash(email.toLowerCase().trim());
    }

    /**
     * Hash de CPF — já deve estar em 11 dígitos sem formatação.
     */
    public String hashCpf(String cpf) {
        return hash(cpf);
    }
}