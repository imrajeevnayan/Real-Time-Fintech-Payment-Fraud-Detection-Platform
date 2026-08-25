package com.aegispay.shared.kernel;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract for every domain event flowing through Aegis Pay.
 * Implementations MUST be Java records and MUST declare a versioned
 * event type name, e.g. {@code aegis.transaction.v1.TransactionRecorded}.
 */
public interface DomainEvent {

    /** Versioned message type name used as Kafka header "eventType". */
    String eventType();

    UUID eventId();

    UUID aggregateId();

    Instant occurredAt();
}
