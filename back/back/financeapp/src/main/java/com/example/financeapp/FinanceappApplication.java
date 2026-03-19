package com.example.financeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMongoRepositories(basePackages = {
		"com.example.financeapp.auth.repository",   // BankerRepository
		"com.example.financeapp.client.repository", // ClientRepository
		"com.example.financeapp.outbox.repository"  // OutboxEventRepository (AGORA NO MONGO!)
})
@EnableJpaRepositories(basePackages = {
		"com.example.financeapp.auth.repository",   // RefreshTokenRepository
		"com.example.financeapp.client.repository"  // ClientAnalyticsRepository
})
public class FinanceappApplication {
	public static void main(String[] args) {
		SpringApplication.run(FinanceappApplication.class, args);
	}
}