package com.aegispay.shared.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Transactional Outbox row. Written in the SAME DB transaction as the
 * aggregate state change; published to Kafka asynchronously by
 * {@link OutboxRelay} (or by a Debezium CDC connector in production).
 */
@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(nullable = false, length = 128)
    private String topic;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String headers;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false)
    private int attempts;

    private OutboxEvent(UUID id, String aggregateType, String aggregateId,
                        String eventType, String topic, String payload, String headers) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.headers = headers;
        this.createdAt = Instant.now();
        this.attempts = 0;
    }

    public static OutboxEvent of(String aggregateType, String aggregateId,
                                 String eventType, String topic, String payloadJson) {
        return new OutboxEvent(UUID.randomUUID(), aggregateType, aggregateId,
                eventType, topic, payloadJson,
                "{\"eventType\":\"%s\"}".formatted(eventType));
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
    }

    public void recordAttempt() {
        this.attempts++;
    }
}
