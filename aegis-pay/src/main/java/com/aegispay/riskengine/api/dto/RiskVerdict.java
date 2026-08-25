package com.aegispay.riskengine.api.dto;

import java.util.List;

/** Structured LLM verdict, mapped from the model's JSON answer. */
public record RiskVerdict(
        int riskScore,
        boolean anomalous,
        List<String> reasons,
        String modelUsed
) {
    public enum Band { LOW, MEDIUM, HIGH }

    public Band band(int highThreshold) {
        if (riskScore >= highThreshold) return Band.HIGH;
        if (riskScore >= highThreshold / 2) return Band.MEDIUM;
        return Band.LOW;
    }
}
