package com.example.financeapp.client.service;

import com.example.financeapp.client.dto.ClientDto;
import com.example.financeapp.client.model.Client;
import com.example.financeapp.client.model.ClientAnalytics;
import com.example.financeapp.client.repository.ClientAnalyticsRepository;
import com.example.financeapp.client.repository.ClientRepository;
import com.example.financeapp.config.encryption.DeterministicHashService;
import com.example.financeapp.exception.ResourceNotFoundException;
import com.example.financeapp.outbox.model.OutboxEvent;
import com.example.financeapp.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;
    private final ClientAnalyticsRepository analyticsRepository;
    private final OutboxEventRepository outboxRepository;
    private final DeterministicHashService hashService;

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<ClientDto.Response> findAll(String bankerId) {
        log.info("Finding all clients for banker: {}", bankerId);

        List<Client> clients = repository.findAllByBankerId(bankerId);
        if (clients.isEmpty()) return List.of();

        List<String> clientIds = clients.stream().map(Client::getId).toList();

        Map<String, ClientAnalytics> analyticsMap = analyticsRepository
                .findAllByMongoClientIdIn(clientIds).stream()
                .collect(Collectors.toMap(ClientAnalytics::getMongoClientId, Function.identity()));

        return clients.stream()
                .map(client -> toResponse(client, analyticsMap.get(client.getId())))
                .toList();
    }

    public ClientDto.Response findById(String id, String bankerId) {
        log.info("Finding client: {}", id);
        Client client = findClientByIdAndBankerId(id, bankerId);
        ClientAnalytics analytics = analyticsRepository.findByMongoClientId(id).orElse(null);
        return toResponse(client, analytics);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public ClientDto.Response create(ClientDto.Request request, String bankerId) {
        log.info("Creating client for banker: {}", bankerId);

        // Verificação de unicidade via hash — funciona mesmo com criptografia
        String emailHash = hashService.hashEmail(request.getEmail());
        String cpfHash   = hashService.hashCpf(request.getCpf());

        if (repository.findByEmailHash(emailHash).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if (repository.findByCpfHash(cpfHash).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        Client client = Client.builder()
                .name(request.getName())
                .email(request.getEmail())      // criptografado pelo EncryptionMongoEventListener
                .cpf(request.getCpf())          // criptografado pelo EncryptionMongoEventListener
                .emailHash(emailHash)           // hash determinístico para buscas futuras
                .cpfHash(cpfHash)               // hash determinístico para buscas futuras
                .phone(request.getPhone())
                .bankerId(bankerId)
                .build();

        Client saved = repository.save(client);

        try {
            outboxRepository.save(OutboxEvent.builder()
                    .eventType("CLIENT_CREATED")
                    .mongoClientId(saved.getId())
                    .build());
        } catch (Exception e) {
            log.error("Failed to persist outbox event for client {}. Rolling back.", saved.getId(), e);
            repository.delete(saved);
            throw new RuntimeException("Client creation failed: could not persist outbox event.", e);
        }

        return toResponse(saved, null);
    }

    public ClientDto.Response update(String id, ClientDto.UpdateRequest request, String bankerId) {
        log.info("Updating client: {}", id);

        Client client = findClientByIdAndBankerId(id, bankerId);

        if (request.getName()  != null) client.setName(request.getName());
        if (request.getPhone() != null) client.setPhone(request.getPhone());

        // Email update: recalcula o hash para manter consistência
        if (request.getEmail() != null) {
            String newEmailHash = hashService.hashEmail(request.getEmail());
            // Verifica se o novo email já pertence a outro cliente
            repository.findByEmailHash(newEmailHash).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Email já cadastrado");
                }
            });
            client.setEmail(request.getEmail());
            client.setEmailHash(newEmailHash);
        }

        Client saved = repository.save(client);
        ClientAnalytics analytics = analyticsRepository.findByMongoClientId(id).orElse(null);
        return toResponse(saved, analytics);
    }

    public void delete(String id, String bankerId) {
        log.info("Deleting client: {}", id);
        Client client = findClientByIdAndBankerId(id, bankerId);
        repository.delete(client);
        try {
            outboxRepository.save(OutboxEvent.builder()
                    .eventType("CLIENT_DELETED")
                    .mongoClientId(id)
                    .build());
        } catch (Exception e) {
            log.error("Client {} deleted but outbox event failed. Analytics may need manual cleanup.", id, e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Client findClientByIdAndBankerId(String id, String bankerId) {
        return repository.findByIdAndBankerId(id, bankerId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private ClientDto.Response toResponse(Client client, ClientAnalytics analytics) {
        return ClientDto.Response.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .cpfMasked(ClientDto.maskCpf(client.getCpf()))
                .phone(client.getPhone())
                .bankerId(client.getBankerId())
                .akropoliLinkId(client.getAkropoliLinkId())
                .connectionStatus(client.getConnectionStatus())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .lastSync(client.getLastSync())
                .analytics(analytics != null ? toAnalyticsResponse(analytics) : null)
                .build();
    }

    private ClientDto.AnalyticsResponse toAnalyticsResponse(ClientAnalytics analytics) {
        return ClientDto.AnalyticsResponse.builder()
                .currentBalance(analytics.getCurrentBalance())
                .projectedBalance(analytics.getProjectedBalance())
                .avgMonthlyIncome(analytics.getAvgMonthlyIncome())
                .avgMonthlyExpenses(analytics.getAvgMonthlyExpenses())
                .incomeVolatility(analytics.getIncomeVolatility())
                .savingsRate(analytics.getSavingsRate())
                .financialHealthScore(analytics.getFinancialHealthScore())
                .topSpendingCategory(analytics.getTopSpendingCategory())
                .build();
    }
}