package com.example.financeapp.outbox.repository;

import com.example.financeapp.outbox.model.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
}
