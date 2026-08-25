package com.aegispay.riskengine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CQRS-lite read model: optimized for "latest risky users" and
 * "was this transaction scored?" queries. Written by the risk-engine,
 * never used for decisions on the write path.
 */
@Entity
@Table(name = "risk_assessments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskAssessmentEntity {

    @Id
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "risk_band", nullable = false, length = 16)
    private String riskBand;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String reasons;

    @Column(name = "model_used", nullable = false, length = 32)
    private String modelUsed;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    public RiskAssessmentEntity(UUID transactionId, String userId, int riskScore,
                                String riskBand, String reasonsJson, String modelUsed) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.riskScore = riskScore;
        this.riskBand = riskBand;
        this.reasons = reasonsJson;
        this.modelUsed = modelUsed;
        this.evaluatedAt = Instant.now();
    }
}
