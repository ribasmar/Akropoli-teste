package com.example.financeapp.config.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter que criptografa/decriptografa campos String
 * automaticamente ao persistir e recuperar entidades do PostgreSQL.
 *
 * Uso — anotar o campo na entidade JPA:
 *
 *   @Convert(converter = EncryptedStringConverter.class)
 *   @Column(name = "cpf", length = 100)   // ciphertext é maior que o plaintext
 *   private String cpf;
 *
 * O converter é transparente para o service — client.getCpf() retorna
 * o CPF em claro, sem que o service saiba que houve criptografia.
 *
 * Tamanho da coluna:
 *   AES-256-GCM com IV de 12 bytes + tag de 16 bytes produz:
 *   Base64( 12 + len(plaintext) + 16 ) ≈ ceil((28 + len) * 4/3) chars
 *   CPF (11 dígitos): ≈ 64 chars → usar length = 100 para folga.
 *   Email (max ~254 chars): ≈ 380 chars → usar length = 512.
 *   akropoliLinkId (string variável): usar length = 512.
 *
 * autoApply = false: converter só é aplicado nos campos anotados com
 * @Convert(converter = ...), não em todos os Strings da aplicação.
 */
@Component
@Converter(autoApply = false)
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    // Spring não injeta em converters JPA por padrão — usamos o holder estático
    // para contornar essa limitação sem perder o gerenciamento do Spring.
    private static FieldEncryptionService encryptionService;

    @Autowired
    void setEncryptionService(FieldEncryptionService service) {
        EncryptedStringConverter.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) return plaintext;
        return encryptionService.encrypt(plaintext);
    }

    @Override
    public String convertToEntityAttribute(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) return ciphertext;
        return encryptionService.decrypt(ciphertext);
    }
}