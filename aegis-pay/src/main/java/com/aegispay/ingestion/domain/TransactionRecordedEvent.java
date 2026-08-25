package com.aegispay.ingestion.domain;

import com.aegispay.shared.kernel.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/** Emitted when a normalized transaction enters the platform. Contract v1. */
public record TransactionRecordedEvent(
        UUID eventId,
        UUID transactionId,
        String userId,
        String merchantId,
        long amountMinor,
        String currency,
        String channel,
        String country,
        String ipAddress,
        String deviceId,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "aegis.transaction.v1.TransactionRecorded";
    }

    @Override
    public UUID aggregateId() {
        return transactionId;
    }
}
