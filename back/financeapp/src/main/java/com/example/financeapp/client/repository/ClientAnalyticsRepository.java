package com.example.financeapp.client.repository;

import com.example.financeapp.client.model.ClientAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAnalyticsRepository extends JpaRepository<ClientAnalytics, String> {

    Optional<ClientAnalytics> findByMongoClientId(String mongoClientId);

    // clientes com saldo projetado negativo (em risco)
    List<ClientAnalytics> findAllByProjectedBalanceLessThan(BigDecimal value);

    // clientes com volatilidade de renda acima de um threshold
    List<ClientAnalytics> findAllByIncomeVolatilityGreaterThan(BigDecimal threshold);

    // ranking por score de saúde financeira
    @Query("SELECT ca FROM ClientAnalytics ca ORDER BY ca.financialHealthScore DESC")
    List<ClientAnalytics> findAllOrderByHealthScore();

    List<ClientAnalytics> findAllByMongoClientIdIn(List<String> mongoClientIds);

    // clientes com taxa de poupança abaixo do recomendado
    List<ClientAnalytics> findAllBySavingsRateLessThan(BigDecimal rate);
}