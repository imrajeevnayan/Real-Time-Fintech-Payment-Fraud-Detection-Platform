package com.aegispay.shared.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Port used by modules to append events to the outbox inside the current
 * JPA transaction. Modules never touch Kafka directly.
 */
@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public void append(String aggregateType, String aggregateId,
                       String eventType, String topic, Object eventPayload) {
        try {
            repository.save(OutboxEvent.of(aggregateType, aggregateId, eventType, topic,
                    objectMapper.writeValueAsString(eventPayload)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize outbox payload for " + eventType, e);
        }
    }
}
