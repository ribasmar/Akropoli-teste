package com.example.financeapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Ativa @CreatedDate e @LastModifiedDate para entidades JPA.
 * O scan de repositórios JPA é declarado em FinanceappApplication.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}