package com.example.financeapp.client.dto;

import com.example.financeapp.client.model.Client.ConnectionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * DTOs do módulo Client.
 */
public class ClientDto {

    // ── Request ───────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "CPF is required")
        @Pattern(regexp = "\\d{11}", message = "CPF must contain 11 digits")
        private String cpf;

        @Pattern(regexp = "\\d{10,11}", message = "Phone must contain 10 or 11 digits")
        private String phone;
    }

    // ── Response padrão (listagens e CRUD) ────────────────────────────────

    /**
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String name;
        private String email;
        private String cpfMasked;
        private String phone;
        private String bankerId;
        private String akropoliLinkId;
        private ConnectionStatus connectionStatus;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastSync;
        private AnalyticsResponse analytics;
    }

    /**
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private String id;
        private String name;
        private String email;
        private String cpf;             // CPF completo — somente neste DTO
        private String phone;
        private String bankerId;
        private String akropoliLinkId; // campo atual de consentimento Open Finance
        private ConnectionStatus connectionStatus;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastSync;
        private AnalyticsResponse analytics;
    }

    // ── UpdateRequest ─────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @Email(message = "Invalid email format")
        private String email;

        @Pattern(regexp = "\\d{10,11}", message = "Phone must contain 10 or 11 digits")
        private String phone;
    }

    // ── AnalyticsResponse ─────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsResponse {
        private BigDecimal currentBalance;
        private BigDecimal projectedBalance;
        private BigDecimal avgMonthlyIncome;
        private BigDecimal avgMonthlyExpenses;
        private BigDecimal incomeVolatility;
        private BigDecimal savingsRate;
        private BigDecimal financialHealthScore;
        private String topSpendingCategory;
    }

    // ── Utilitário de máscara ─────────────────────────────────────────────

    /**
     */
    public static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "***.***.***-**";
        }
        // Expõe apenas dígitos 3-8 (posições 3,4,5,6,7,8 — índice base-0)
        return "***" + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}