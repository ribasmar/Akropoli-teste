package com.example.financeapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Ativa @CreatedDate e @LastModifiedDate para entidades MongoDB (Banker, Client).
 * O scan de repositórios MongoDB é declarado em FinanceappApplication.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}