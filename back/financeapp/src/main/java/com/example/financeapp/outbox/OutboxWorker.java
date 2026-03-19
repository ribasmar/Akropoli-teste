package com.example.financeapp.outbox;

import com.example.financeapp.client.model.ClientAnalytics;
import com.example.financeapp.client.repository.ClientAnalyticsRepository;
import com.example.financeapp.outbox.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {

    private static final int MAX_RETRIES = 5;

    // Usamos o MongoTemplate para acesso a queries atómicas avançadas
    private final MongoTemplate mongoTemplate;
    private final ClientAnalyticsRepository analyticsRepository;

    @Scheduled(fixedDelay = 10_000)
    public void processPendingEvents() {
        // Drena a fila: processa eventos até que não haja mais nenhum PENDING
        OutboxEvent event;
        while ((event = fetchNextEventAtomically()) != null) {
            try {
                process(event);

                // Marca como sucesso
                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                mongoTemplate.save(event);

                log.info("Outbox event {} ({}) processed successfully.", event.getId(), event.getEventType());
            } catch (Exception e) {
                // Em caso de falha, incrementa retry e volta para PENDING (ou FAILED se atingiu o limite)
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus("FAILED");
                    log.error("Outbox event {} FAILED permanently after {} retries: {}", event.getId(), MAX_RETRIES, e.getMessage());
                } else {
                    event.setStatus("PENDING");
                    log.warn("Outbox event {} retry {}/{}: {}", event.getId(), event.getRetryCount(), MAX_RETRIES, e.getMessage());
                }
                mongoTemplate.save(event);
            }
        }
    }

    /**
     * O equivalente ao "FOR UPDATE SKIP LOCKED" para o MongoDB.
     * Encontra atómicamente um evento PENDING e altera logo o status para PROCESSING.
     * Se 3 instâncias correrem isto ao mesmo tempo, o MongoDB garante que não apanham o mesmo.
     */
    private OutboxEvent fetchNextEventAtomically() {
        Query query = new Query(
                Criteria.where("status").is("PENDING")
                        .and("retryCount").lt(MAX_RETRIES)
        );
        // Processa os mais antigos primeiro
        query.with(Sort.by(Sort.Direction.ASC, "createdAt"));

        // Altera para PROCESSING imediatamente para que outros workers o ignorem
        Update update = new Update().set("status", "PROCESSING");

        return mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true), // Devolve o objeto JÁ alterado
                OutboxEvent.class
        );
    }

    private void process(OutboxEvent event) {
        switch (event.getEventType()) {
            case "CLIENT_CREATED" -> {
                if (analyticsRepository.findByMongoClientId(event.getMongoClientId()).isEmpty()) {
                    analyticsRepository.save(
                            ClientAnalytics.builder()
                                    .mongoClientId(event.getMongoClientId())
                                    .build()
                    );
                }
            }
            case "CLIENT_DELETED" -> {
                analyticsRepository.findByMongoClientId(event.getMongoClientId())
                        .ifPresent(analyticsRepository::delete);
            }
            default -> log.warn("Unknown outbox event type: {}", event.getEventType());
        }
    }
}