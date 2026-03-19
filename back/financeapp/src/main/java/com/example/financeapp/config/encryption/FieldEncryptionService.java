package com.example.financeapp.config.encryption;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Serviço de criptografia de campos sensíveis usando AES-256-GCM.
 *
 * Formato do valor armazenado (Base64):
 *   [ IV (12 bytes) ][ ciphertext + GCM auth tag (16 bytes) ]
 *   Tudo concatenado e codificado em Base64 URL-safe para armazenamento como String.
 *
 * IV (Initialization Vector):
 *   Gerado aleatoriamente a cada encrypt(). Isso garante que o mesmo plaintext
 *   produza ciphertexts diferentes — essencial para que não seja possível
 *   inferir se dois clientes têm o mesmo CPF comparando os valores cifrados.
 *   O IV não é secreto — é armazenado junto com o ciphertext.
 *
 * Chave:
 *   AES-256 exige 32 bytes. A chave vem de ENCRYPTION_KEY no .env,
 *   codificada em Base64 (44 chars). Gerar com:
 *     openssl rand -base64 32
 *
 * Rotação de chave:
 *   Para rotacionar a chave, adicionar ENCRYPTION_KEY_OLD ao .env,
 *   implementar decrypt() com fallback para a chave antiga, e rodar
 *   um job de migração que re-encripta todos os campos.
 */
@Slf4j
@Component
public class FieldEncryptionService {

    private static final String ALGORITHM    = "AES/GCM/NoPadding";
    private static final int    IV_LENGTH    = 12;   // 96 bits — recomendado pelo NIST para GCM
    private static final int    TAG_LENGTH   = 128;  // bits — tag de autenticação GCM

    @Value("${encryption.key}")
    private String encryptionKeyBase64;

    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    void init() {
        byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY must be exactly 32 bytes (256 bits) encoded as Base64. " +
                            "Generate with: openssl rand -base64 32"
            );
        }
        secretKey = new SecretKeySpec(keyBytes, "AES");
        log.info("Field encryption initialized (AES-256-GCM)");
    }

    /**
     * Criptografa um valor em claro.
     *
     * @param plaintext valor a criptografar (nunca null — verificar antes de chamar)
     * @return Base64 contendo IV + ciphertext + GCM tag
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,
                    new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // Concatena IV + ciphertext e codifica em Base64
            byte[] combined = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(ciphertext, 0, combined, IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt field", e);
        }
    }

    /**
     * Decriptografa um valor armazenado.
     *
     * @param cipherBase64 valor retornado pelo banco (IV + ciphertext em Base64)
     * @return plaintext original
     */
    public String decrypt(String cipherBase64) {
        if (cipherBase64 == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(cipherBase64);

            byte[] iv         = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey,
                    new GCMParameterSpec(TAG_LENGTH, iv));

            return new String(cipher.doFinal(ciphertext), "UTF-8");

        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt field — key mismatch or data corruption", e);
        }
    }

    // ── Exceção específica para não vazar stack traces com dados sensíveis ───

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}