package com.aegispay.riskengine.domain;

import com.aegispay.shared.kernel.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Emitted when a transaction scores above the risk threshold. Contract v1. */
public record HighRiskTransactionDetectedEvent(
        UUID eventId,
        UUID transactionId,
        String userId,
        int riskScore,
        String riskBand,
        List<String> reasons,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "aegis.risk.v1.HighRiskTransactionDetected";
    }

    @Override
    public UUID aggregateId() {
        return transactionId;
    }
}
