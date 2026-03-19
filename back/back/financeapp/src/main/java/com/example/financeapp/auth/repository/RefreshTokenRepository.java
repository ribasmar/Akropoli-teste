package com.example.financeapp.auth.repository;

import com.example.financeapp.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /** Busca um token pelo seu hash SHA-256 — usado na validação do /refresh. */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revoga todos os tokens ativos de um banker — usado no logout total. */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.bankerId = :bankerId AND rt.revokedAt IS NULL")
    int revokeAllByBankerId(@Param("bankerId") String bankerId, @Param("now") LocalDateTime now);

    /** Remove tokens expirados — para limpeza periódica via @Scheduled. */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :threshold")
    int deleteExpiredTokens(@Param("threshold") LocalDateTime threshold);
}