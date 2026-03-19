package com.example.financeapp.client.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 *
 * Algoritmo — Dígito Verificador 1 (10º dígito):
 *   1. Multiplica os 9 primeiros dígitos pelos pesos 10, 9, 8, ..., 2
 *   2. Soma os produtos
 *   3. Calcula o resto da divisão por 11
 *   4. Se resto < 2: dígito = 0; caso contrário: dígito = 11 - resto
 *
 * Algoritmo — Dígito Verificador 2 (11º dígito):
 *   1. Multiplica os 10 primeiros dígitos pelos pesos 11, 10, 9, ..., 2
 *   2. Mesma lógica de cálculo
 *
 * Rejeições adicionais:
 *   - Sequências homogêneas: "00000000000" a "99999999999" são matematicamente
 *     válidas pelo algoritmo mas são CPFs inválidos na prática.
 */
public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        // null é tratado por @NotBlank — aqui apenas passamos
        if (cpf == null) {
            return true;
        }

        // Deve ter exatamente 11 dígitos numéricos
        if (!cpf.matches("\\d{11}")) {
            return false;
        }

        // Rejeita sequências homogêneas ("00000000000", "11111111111" etc.)
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // Valida 1º dígito verificador (posição 9)
        if (!isDigitValid(cpf, 9)) {
            return false;
        }

        // Valida 2º dígito verificador (posição 10)
        return isDigitValid(cpf, 10);
    }

    /**
     * Valida um dígito verificador do CPF na posição informada (9 ou 10).
     *
     * @param cpf      string com 11 dígitos
     * @param position posição do dígito a validar (índice base-0: 9 ou 10)
     */
    private boolean isDigitValid(String cpf, int position) {
        int sum = 0;
        int weight = position + 1; // peso inicial: 10 para posição 9, 11 para posição 10

        for (int i = 0; i < position; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (weight - i);
        }

        int remainder = sum % 11;
        int expectedDigit = (remainder < 2) ? 0 : (11 - remainder);
        int actualDigit   = Character.getNumericValue(cpf.charAt(position));

        return actualDigit == expectedDigit;
    }
}