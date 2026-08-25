package com.aegispay.shared.kernel;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Canonical, strictly versioned Kafka topics. Bumping a message contract
 * means adding a new topic (aegis.transaction.v2), never mutating v1.
 */
@ConfigurationProperties(prefix = "aegis.topics")
public record Topics(String transactionRecorded, String riskEvaluated, String remediationExecuted) {
}
