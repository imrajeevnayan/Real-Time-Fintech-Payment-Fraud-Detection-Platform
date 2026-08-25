package com.aegispay.ingestion.application;

import com.aegispay.ingestion.api.dto.SubmitTransactionRequest;
import com.aegispay.ingestion.domain.Transaction;
import com.aegispay.ingestion.domain.TransactionRecordedEvent;
import com.aegispay.ingestion.domain.TransactionRepository;
import com.aegispay.shared.kernel.Topics;
import com.aegispay.shared.outbox.OutboxWriter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hexagonal application service: normalizes the inbound payload, persists the
 * Transaction aggregate and appends a TransactionRecorded event to the outbox —
 * atomically. The risk-engine reacts to the Kafka topic, never in-process.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final TransactionRepository transactions;
    private final OutboxWriter outbox;
    private final Topics topics;

    @Transactional
    public UUID submit(SubmitTransactionRequest request) {
        var transaction = Transaction.newTransaction(
                UUID.randomUUID(),
                request.userId(),
                request.merchantId(),
                request.amount(),
                request.currency(),
                request.channel(),
                request.country(),
                request.ipAddress(),
                request.deviceId());

        transactions.save(transaction);

        var event = new TransactionRecordedEvent(
                UUID.randomUUID(), transaction.getId(), transaction.getUserId(),
                transaction.getMerchantId(), transaction.getAmountMinor(),
                transaction.getCurrency(), transaction.getChannel(), transaction.getCountry(),
                transaction.getIpAddress(), transaction.getDeviceId(), transaction.getOccurredAt());

        outbox.append("Transaction", transaction.getId().toString(),
                event.eventType(), topics.transactionRecorded(), event);

        log.debug("Ingested transaction {} for user {}", transaction.getId(), transaction.getUserId());
        return transaction.getId();
    }
}
