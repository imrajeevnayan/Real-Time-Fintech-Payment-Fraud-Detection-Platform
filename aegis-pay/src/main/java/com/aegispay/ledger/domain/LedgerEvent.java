package com.aegispay.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only ledger entry. Every Kafka event from every module lands here
 * (via the ledger projection consumer), giving a replayable, event-sourced
 * audit trail of all transactions and state changes.
 */
@Entity
@Table(name = "ledger_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private java.time.Instant occurredAt;

    public LedgerEvent(String aggregateType, String aggregateId, String eventType, String payloadJson) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payloadJson;
        this.occurredAt = java.time.Instant.now();
    }
}
