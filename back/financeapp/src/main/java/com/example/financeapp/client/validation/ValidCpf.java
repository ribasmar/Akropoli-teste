package com.example.financeapp.client.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Aceita null (use @NotBlank em conjunto para campos obrigatórios).
 * Aceita apenas CPFs em formato de 11 dígitos sem formatação ("12345678901").
 *
 * A validação implementa o algoritmo oficial da Receita Federal:
 *   - Calcula o 10º dígito verificador
 *   - Calcula o 11º dígito verificador
 *   - Rejeita sequências homogêneas (ex: "00000000000", "11111111111")
 */
@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCpf {
    String message() default "CPF inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}