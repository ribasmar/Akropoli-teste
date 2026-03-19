package com.example.financeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da aplicação.
 *
 * Com dois módulos Spring Data ativos (MongoDB + JPA), o framework entra
 * em "strict mode" e exige que cada módulo saiba exatamente quais pacotes
 * escanear. Apontar ambos para o pacote raiz fazia o Spring tentar atribuir
 * cada repositório aos dois stores ao mesmo tempo, gerando os warns de
 * ambiguidade e o BeanDefinitionOverrideException.
 *
 * Solução: basePackages explícitos e separados para cada módulo.
 *
 * MongoDB  → auth (BankerRepository) + client (ClientRepository)
 * JPA      → auth (RefreshTokenRepository) + client (ClientAnalyticsRepository)
 *            + outbox (OutboxEventRepository)
 *
 * O @EnableMongoAuditing e @EnableJpaAuditing ficam em MongoConfig e
 * JpaConfig respectivamente — sem @Enable*Repositories neles.
 */
@SpringBootApplication
@EnableScheduling
@EnableMongoRepositories(basePackages = {
		"com.example.financeapp.auth.repository",   // BankerRepository
		"com.example.financeapp.client.repository"  // ClientRepository
})
@EnableJpaRepositories(basePackages = {
		"com.example.financeapp.auth.repository",   // RefreshTokenRepository
		"com.example.financeapp.client.repository", // ClientAnalyticsRepository
		"com.example.financeapp.outbox.repository"  // OutboxEventRepository
})
public class FinanceappApplication {
	public static void main(String[] args) {
		SpringApplication.run(FinanceappApplication.class, args);
	}
}