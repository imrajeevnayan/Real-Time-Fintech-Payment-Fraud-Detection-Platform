package com.aegispay.riskengine.application;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes aegis.transaction.v1 (published by the ingestion outbox) and
 * drives the AI evaluation. Idempotent: a re-delivered event simply
 * re-evaluates and overwrites the read model projection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionRecordedConsumer {

    private final RiskEvaluationService riskEngine;

    @KafkaListener(topics = "${aegis.topics.transaction-recorded}", groupId = "aegis-risk-engine")
    public void onTransactionRecorded(JsonNode event) {
        try {
            riskEngine.evaluate(
                    java.util.UUID.fromString(event.path("transactionId").asText()),
                    event.path("userId").asText(),
                    event.path("amountMinor").asLong(),
                    event.path("currency").asText(),
                    event.path("channel").asText(),
                    event.hasNonNull("country") ? event.path("country").asText() : null,
                    event.hasNonNull("ipAddress") ? event.path("ipAddress").asText() : null,
                    event.hasNonNull("merchantId") ? event.path("merchantId").asText() : null,
                    event.path("occurredAt").asText());
        } catch (Exception e) {
            // Do not retry forever: score is default-recorded; alerting would pick this up.
            log.error("Risk evaluation failed for event {}", event.path("transactionId").asText(), e);
        }
    }
}
