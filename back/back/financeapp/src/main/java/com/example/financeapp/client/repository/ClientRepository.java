package com.example.financeapp.client.repository;

import com.example.financeapp.client.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String> {

    Optional<Client> findByIdAndBankerId(String id, String bankerId);

    List<Client> findAllByBankerId(String bankerId);

    /**
     * Busca por email usando o hash determinístico (HMAC-SHA256).
     * pois ciphertexts são diferentes a cada encrypt() (IV aleatório).
     *
     * Uso: repository.findByEmailHash(hashService.hashEmail(email))
     */
    Optional<Client> findByEmailHash(String emailHash);

    /**
     * Busca por CPF usando o hash determinístico (HMAC-SHA256).
     *
     * Uso: repository.findByCpfHash(hashService.hashCpf(cpf))
     */
    Optional<Client> findByCpfHash(String cpfHash);

    /**
     * Lookup pelo linkId da Akropoli — usado pelos webhooks.
     */
    Optional<Client> findByAkropoliLinkId(String akropoliLinkId);

    /**
     * @deprecated Use findByAkropoliLinkId.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    Optional<Client> findByPluggyItemId(String pluggyItemId);
}