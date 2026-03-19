package com.example.financeapp.client.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_analytics")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // referência ao Client do MongoDB
    @Column(name = "mongo_client_id", nullable = false, unique = true)
    private String mongoClientId;

    // saldo atual consolidado
    @Column(precision = 15, scale = 2)
    private BigDecimal currentBalance;

    // saldo projetado para os próximos 30 dias
    @Column(precision = 15, scale = 2)
    private BigDecimal projectedBalance;

    // média de receita mensal
    @Column(precision = 15, scale = 2)
    private BigDecimal avgMonthlyIncome;

    // média de despesas mensais
    @Column(precision = 15, scale = 2)
    private BigDecimal avgMonthlyExpenses;

    // volatilidade de renda (desvio padrão)
    @Column(precision = 10, scale = 4)
    private BigDecimal incomeVolatility;

    // maior categoria de gasto
    private String topSpendingCategory;

    // taxa de poupança (%)
    @Column(precision = 5, scale = 2)
    private BigDecimal savingsRate;

    // score de saúde financeira (0-100)
    @Column(precision = 5, scale = 2)
    private BigDecimal financialHealthScore;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}