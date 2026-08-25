package com.aegispay.shared.outbox;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Debezium-style outbox relay. In production a Debezium PostgreSQL connector
 * tails the outbox table via CDC and publishes to Kafka with lower latency;
 * this poller is the always-available fallback (and the dev/test path).
 * At-least-once delivery: rows are marked published only AFTER a
 * successful, idempotent Kafka send.
 */
@Slf4j
@Component
public class OutboxRelay {

    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final int batchSize;
    private final AtomicBoolean draining = new AtomicBoolean(false);

    public OutboxRelay(OutboxEventRepository outbox,
                       KafkaTemplate<String, String> kafka,
                       @Value("${aegis.outbox.relay.batch-size:100}") int batchSize) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${aegis.outbox.relay.poll-interval:500ms}")
    public void drain() {
        if (!draining.compareAndSet(false, true)) {
            return; // previous tick still in flight; virtual threads make re-entry cheap but pointless
        }
        try {
            relayBatch();
        } finally {
            draining.set(false);
        }
    }

    @Transactional
    void relayBatch() {
        var batch = outbox.lockNextBatch(batchSize);
        for (OutboxEvent event : batch) {
            try {
                // key = aggregate id => per-transaction ordering preserved inside a partition.
                // Blocking send on a virtual thread is fine and guarantees markPublished()
                // is visible before this transaction commits (at-least-once).
                kafka.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get();
                event.markPublished();
            } catch (Exception e) {
                event.recordAttempt();
                log.error("Outbox publish failed for {} (attempt {}), will retry next poll",
                        event.getEventType(), event.getAttempts(), e);
            }
        }
    }
}
