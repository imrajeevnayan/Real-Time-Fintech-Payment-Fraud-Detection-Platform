package com.aegispay.riskengine.rag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * RAG pipeline over PgVector.
 * Write side: each confirmed transaction is embedded and stored with
 * metadata {userId, amountMinor, ...} so similarity search + metadata
 * filtering can express "this user's recent behavior".
 * Read side: nearest-neighbour retrieval feeds the LLM prompt with
 * the user's own history (and coarse cross-user signals).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserHistoryVectorService {

    private static final int TOP_K = 10;

    private final VectorStore vectorStore;

    /** Embed and persist a single historical transaction for a user. */
    public void indexTransaction(UUID transactionId, String userId, long amountMinor,
                                 String currency, String channel, String country,
                                 String merchantId, String occurredAtIso) {
        var content = """
                Transaction by user %s: amount %d %s via %s in %s at merchant %s on %s
                """.formatted(userId, amountMinor, currency, channel,
                country == null ? "unknown" : country, merchantId == null ? "unknown" : merchantId,
                occurredAtIso);
        var doc = new Document(transactionId.toString(), content, Map.of(
                "userId", userId,
                "amountMinor", amountMinor,
                "currency", currency,
                "channel", channel,
                "country", country == null ? "unknown" : country));
        vectorStore.add(List.of(doc));
    }

    /**
     * Retrieve the most relevant historical transactions for this user.
     * The query text embeds the *new* transaction, so "similarity" here means
     * "how close is this new behavior to the user's established pattern".
     */
    public List<String> retrieveSimilarHistory(String userId, String newTransactionDescription) {
        var results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(newTransactionDescription)
                .topK(TOP_K)
                .filterExpression("userId == '" + userId + "'")
                .build());
        return results.stream()
                .map(result -> result.getContent() + " (similarity %.2f)"
                        .formatted(result.getScore()))
                .toList();
    }

    /** Drop a user's history (e.g. after an account closure / rebaseline). */
    public void purgeUserHistory(String userId) {
        vectorStore.delete("userId == '" + userId + "'");
    }
}
