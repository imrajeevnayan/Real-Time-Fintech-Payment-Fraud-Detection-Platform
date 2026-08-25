package com.aegispay.remediation.domain;

import com.aegispay.shared.kernel.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Remediation write model. State transitions are guarded — an already-frozen
 * account cannot be re-frozen (idempotency for Kafka redeliveries).
 */
@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends AggregateRoot {

    public enum Status { ACTIVE, UNDER_REVIEW, FROZEN }

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;

    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private java.time.Instant updatedAt;

    protected Account(UUID id, String userId, Status status) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.updatedAt = java.time.Instant.now();
    }

    public static Account open(UUID id, String userId) {
        return new Account(id, userId, Status.ACTIVE);
    }

    /** @return true if this call caused a transition, false if already frozen. */
    public boolean freeze() {
        if (status == Status.FROZEN) {
            return false;
        }
        this.status = Status.FROZEN;
        this.updatedAt = java.time.Instant.now();
        this.version++;
        return true;
    }

    /** @return true if this call caused a transition, false if already under review. */
    public boolean flagForReview() {
        if (status != Status.ACTIVE) {
            return false;
        }
        this.status = Status.UNDER_REVIEW;
        this.updatedAt = java.time.Instant.now();
        this.version++;
        return true;
    }
}
