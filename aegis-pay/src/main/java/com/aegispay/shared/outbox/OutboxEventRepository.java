package com.aegispay.shared.outbox;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * SKIP LOCKED so multiple relay instances can drain the outbox concurrently
     * (same technique Debezium mitigates with a single connector per table).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE published_at IS NULL
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(int limit);

    Optional<OutboxEvent> findByIdAndPublishedAtIsNull(UUID id);

    long countByPublishedAtIsNullAndCreatedAtBefore(Instant cutoff);
}
