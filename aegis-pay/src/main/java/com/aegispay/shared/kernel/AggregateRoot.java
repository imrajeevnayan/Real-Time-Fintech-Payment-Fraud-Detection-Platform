package com.aegispay.shared.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Base class for write-model aggregates (CQRS-lite command side).
 * Domain events are registered here and persisted atomically with the
 * aggregate state via the transactional outbox — never published directly.
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    protected void raise(DomainEvent event) {
        pendingEvents.add(event);
    }

    public List<DomainEvent> pullPendingEvents() {
        var events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
