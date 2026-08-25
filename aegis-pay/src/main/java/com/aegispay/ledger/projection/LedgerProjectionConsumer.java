package com.aegispay.ledger.projection;

import com.aegispay.ledger.domain.LedgerEvent;
import com.aegispay.ledger.domain.LedgerEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Ledger projection: subscribes to every versioned Aegis topic and appends
 * to the immutable ledger_events table. Duplicate appends are acceptable
 * (at-least-once); production dedup would use the eventId in a unique index.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerProjectionConsumer {

    private final LedgerEventRepository ledger;

    @KafkaListener(
            topics = {"${aegis.topics.transaction-recorded}",
                      "${aegis.topics.risk-evaluated}",
                      "${aegis.topics.remediation-executed}"},
            groupId = "aegis-ledger")
    public void onDomainEvent(JsonNode event,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        var aggregateId = event.path("transactionId").asText(event.path("userId").asText("unknown"));
        ledger.save(new LedgerEvent("Transaction", aggregateId, topic, event.toString()));
    }
}
