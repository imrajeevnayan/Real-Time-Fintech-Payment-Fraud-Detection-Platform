package com.aegispay.remediation.application;

import com.aegispay.remediation.domain.Account;
import com.aegispay.remediation.domain.AccountRepository;
import com.aegispay.remediation.domain.RemediationExecutedEvent;
import com.aegispay.shared.kernel.Topics;
import com.aegispay.shared.outbox.OutboxWriter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies autonomous remediation to the Account aggregate. All actions go
 * through the outbox so downstream systems (ledger, notifications, case
 * management) see the same truth.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemediationService {

    private final AccountRepository accounts;
    private final OutboxWriter outbox;
    private final Topics topics;

    @Transactional
    public void handleHighRisk(UUID transactionId, String userId, int riskScore) {
        var account = accounts.findByUserId(userId)
                .orElseGet(() -> accounts.save(Account.open(UUID.randomUUID(), userId)));

        boolean changed = riskScore >= 90 ? account.freeze() : account.flagForReview();
        if (!changed) {
            log.info("Account for user {} already in state {}, no transition applied", userId, account.getStatus());
            return;
        }

        var event = new RemediationExecutedEvent(UUID.randomUUID(), transactionId, userId,
                account.getStatus() == Account.Status.FROZEN ? "FREEZE_ACCOUNT" : "SUSPEND_CARD",
                account.getStatus().name(), java.time.Instant.now());
        outbox.append("Account", account.getId().toString(),
                event.eventType(), topics.remediationExecuted(), event);

        log.warn("REMEDIATION: user {} → {} (risk {}, tx {})",
                userId, account.getStatus(), riskScore, transactionId);
        // TODO out-of-band: notify user via push/SMS gateway (NOTIFY_USER action).
    }
}
