package com.example.financeapp.auth.repository;

import com.example.financeapp.auth.model.Banker;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankerRepository extends MongoRepository<Banker, String> {

    /**
     * Busca por email usando o hash determinístico (HMAC-SHA256).
     */
    Optional<Banker> findByEmailHash(String emailHash);

    /**
     * Verifica existência por email via hash.
     */
    boolean existsByEmailHash(String emailHash);
}