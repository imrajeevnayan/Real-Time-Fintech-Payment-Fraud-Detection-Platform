package com.aegispay.remediation.domain;

import com.aegispay.shared.kernel.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/** Emitted when an autonomous remediation action lands. Contract v1. */
public record RemediationExecutedEvent(
        UUID eventId,
        UUID transactionId,
        String userId,
        String action,          // FREEZE_ACCOUNT | SUSPEND_CARD | NOTIFY_USER
        String resultingStatus,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "aegis.remediation.v1.RemediationExecuted";
    }

    @Override
    public UUID aggregateId() {
        return transactionId;
    }
}
