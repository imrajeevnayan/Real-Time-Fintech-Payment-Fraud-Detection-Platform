package com.aegispay.remediation.application;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes aegis.risk.v1 HighRiskTransactionDetected events and applies
 * state changes to the Account aggregate. Manual ack AFTER the state change
 * commits; a crash mid-processing replays the message and hits the
 * aggregate's idempotent transition guards.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HighRiskTransactionConsumer {

    private final RemediationService remediation;

    @KafkaListener(topics = "${aegis.topics.risk-evaluated}", groupId = "aegis-remediation")
    public void onHighRiskTransactionDetected(JsonNode event, Acknowledgment ack) {
        try {
            remediation.handleHighRisk(
                    UUID.fromString(event.path("transactionId").asText()),
                    event.path("userId").asText(),
                    event.path("riskScore").asInt());
        } catch (Exception e) {
            log.error("Remediation failed for tx {} — event will be re-delivered",
                    event.path("transactionId").asText(), e);
            return; // don't ack; Kafka redelivers and the aggregate guard keeps us idempotent
        }
        ack.acknowledge();
    }
}
