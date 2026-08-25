package com.aegispay.remediation;

import static org.assertj.core.api.Assertions.assertThat;

import com.aegispay.remediation.domain.Account;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountTest {

    @Test
    void freezeIsIdempotent() {
        var account = Account.open(UUID.randomUUID(), "user-1");
        assertThat(account.freeze()).isTrue();
        assertThat(account.getStatus()).isEqualTo(Account.Status.FROZEN);
        assertThat(account.freeze()).isFalse(); // Kafka redelivery must be a no-op
        assertThat(account.getVersion()).isEqualTo(1);
    }

    @Test
    void reviewOnlyAppliesToActiveAccounts() {
        var account = Account.open(UUID.randomUUID(), "user-2");
        assertThat(account.freeze()).isTrue();
        assertThat(account.flagForReview()).isFalse(); // frozen stays frozen
    }
}
