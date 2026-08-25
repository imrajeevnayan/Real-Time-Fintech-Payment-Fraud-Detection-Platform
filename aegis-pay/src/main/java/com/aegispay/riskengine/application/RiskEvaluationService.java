package com.aegispay.riskengine.application;

import com.aegispay.riskengine.ai.IpReputationFunction;
import com.aegispay.riskengine.api.dto.RiskVerdict;
import com.aegispay.riskengine.domain.HighRiskTransactionDetectedEvent;
import com.aegispay.riskengine.domain.RiskAssessmentEntity;
import com.aegispay.riskengine.rag.UserHistoryVectorService;
import com.aegispay.shared.kernel.Topics;
import com.aegispay.shared.outbox.OutboxWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Two-stage LLM risk scoring:
 *   1. FAST pass on local Ollama — costs ~nothing, filters the obvious.
 *   2. DEEP pass on OpenAI with RAG context + tool calling — only when the
 *      fast score is ambiguous. This keeps p99 latency sane and LLM spend low.
 * The verdict is persisted as a read model projection and (when high-risk)
 * appended to the outbox for the remediation module.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskEvaluationService {

    private final ChatClient fastChatClient;
    private final ChatClient deepChatClient;
    private final UserHistoryVectorService vectorService;
    private final RiskAssessmentRepository assessments;
    private final OutboxWriter outbox;
    private final Topics topics;

    @Value("${aegis.risk-engine.threshold:75}")
    private int riskThreshold;

    @Value("${aegis.risk-engine.fast-model-max-score:40}")
    private int fastModelMaxScore;

    @Transactional
    public RiskVerdict evaluate(UUID transactionId, String userId, long amountMinor, String currency,
                                String channel, String country, String ipAddress, String merchantId,
                                String occurredAtIso) {

        var newTxDescription = describe(transactionId, userId, amountMinor, currency, channel,
                country, merchantId, occurredAtIso);

        RiskVerdict verdict = fastPass(newTxDescription);
        if (verdict.riskScore() > fastModelMaxScore) {
            verdict = deepPassWithRag(newTxDescription, userId, ipAddress);
        }

        var band = verdict.band(riskThreshold);
        assessments.save(new RiskAssessmentEntity(
                transactionId, userId, verdict.riskScore(), band.name(),
                toJsonArray(verdict.reasons()), verdict.modelUsed()));

        if (band == RiskVerdict.Band.HIGH) {
            var event = new HighRiskTransactionDetectedEvent(UUID.randomUUID(), transactionId,
                    userId, verdict.riskScore(), band.name(), verdict.reasons(), Instant.now());
            outbox.append("Transaction", transactionId.toString(),
                    event.eventType(), topics.riskEvaluated(), event);
        }

        // Feed the (assumed-legitimate-until-proven-guilty) transaction back into RAG.
        // Remediation outcomes would refine this in a production system.
        vectorService.indexTransaction(transactionId, userId, amountMinor, currency,
                channel, country, merchantId, occurredAtIso);

        log.info("Transaction {} scored {} ({}, {})", transactionId, verdict.riskScore(),
                band, verdict.modelUsed());
        return verdict;
    }

    private RiskVerdict fastPass(String newTxDescription) {
        String json = fastChatClient.prompt()
                .user("Triage this transaction for fraud risk: " + newTxDescription)
                .call()
                .content();
        return parseVerdict(json, "ollama-llama3.1");
    }

    private RiskVerdict deepPassWithRag(String newTxDescription, String userId, String ipAddress) {
        List<String> history = vectorService.retrieveSimilarHistory(userId, newTxDescription);

        String prompt = """
                Is this new transaction anomalous based on this user's history?

                NEW TRANSACTION:
                %s

                THE USER'S 10 MOST SIMILAR HISTORICAL TRANSACTIONS (from vector search):
                %s

                Steps:
                1. Compare the new transaction against the history (amount, merchant, channel, geography).
                2. Call the ipReputation tool for the transaction's IP and factor the result in.
                3. Return your JSON verdict.
                """.formatted(newTxDescription,
                history.isEmpty() ? "(no prior history — treat as elevated baseline risk)" :
                        String.join("\n", history));

        String json = deepChatClient.prompt().user(prompt).call().content();
        return parseVerdict(json, "openai-gpt-4o");
    }

    private String describe(UUID transactionId, String userId, long amountMinor, String currency,
                            String channel, String country, String merchantId, String occurredAtIso) {
        return "tx=%s user=%s amount=%d %s channel=%s country=%s merchant=%s at=%s"
                .formatted(transactionId, userId, amountMinor, currency, channel,
                        country == null ? "unknown" : country,
                        merchantId == null ? "unknown" : merchantId, occurredAtIso);
    }

    private RiskVerdict parseVerdict(String rawJson, String modelUsed) {
        // Strip optional markdown fences; Jackson does the strict mapping.
        var cleaned = rawJson == null ? "" : rawJson.replaceAll("(?s)^```(json)?|```$", "").trim();
        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(cleaned);
            return new RiskVerdict(
                    Math.clamp(node.path("riskScore").asInt(0), 0, 100),
                    node.path("anomalous").asBoolean(false),
                    List.of(node.path("reasons").toString().replaceAll("[\\[\\]\"]", "").split(",")),
                    modelUsed);
        } catch (Exception e) {
            log.warn("Unparseable LLM verdict ({}), failing closed to score 50", modelUsed, e);
            return new RiskVerdict(50, true, List.of("model output unparseable — manual review"), modelUsed);
        }
    }

    private String toJsonArray(List<String> reasons) {
        return reasons.toString(); // stored as jsonb array string
    }
}
