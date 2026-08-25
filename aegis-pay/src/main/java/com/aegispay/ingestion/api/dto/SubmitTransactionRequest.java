package com.aegispay.ingestion.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Inbound REST/WebSocket payload for a card-not-present transaction. */
public record SubmitTransactionRequest(

        @NotBlank @Size(max = 64)
        String userId,

        String merchantId,

        @NotNull @DecimalMin("0.00")
        BigDecimal amount,

        @NotBlank @Pattern(regexp = "[A-Z]{3}")
        String currency,

        @NotBlank @Pattern(regexp = "ECOMMERCE|POS|ATM|P2P")
        String channel,

        @Pattern(regexp = "[A-Z]{2}")
        String country,

        @Pattern(regexp = "^[0-9a-fA-F.:]{3,45}$")
        String ipAddress,

        String deviceId
) {
}
