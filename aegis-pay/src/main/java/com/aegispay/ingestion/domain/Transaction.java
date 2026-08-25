package com.aegispay.ingestion.domain;

import com.aegispay.shared.kernel.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Ingestion write model. Money is stored as minor units (cents) to avoid FP drift. */
@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends AggregateRoot {

    public enum Status { PENDING_EVALUATION, EVALUATED, FLAGGED, BLOCKED }

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "merchant_id", length = 64)
    private String merchantId;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(length = 2)
    private String country;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(nullable = false)
    private long version;

    private Transaction(UUID id, String userId, String merchantId, long amountMinor, String currency,
                        String channel, String country, String ipAddress, String deviceId,
                        Status status, Instant occurredAt) {
        this.id = id;
        this.userId = userId;
        this.merchantId = merchantId;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.channel = channel;
        this.country = country;
        this.ipAddress = ipAddress;
        this.deviceId = deviceId;
        this.status = status;
        this.occurredAt = occurredAt;
        this.recordedAt = Instant.now();
        this.version = 0;
    }

    public static Transaction newTransaction(UUID id, String userId, String merchantId,
                                             BigDecimal amount, String currency, String channel,
                                             String country, String ipAddress, String deviceId) {
        var tx = new Transaction(id, userId, merchantId,
                amount.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact(),
                currency, channel, country, ipAddress, deviceId,
                Status.PENDING_EVALUATION, Instant.now());
        return tx;
    }
}
