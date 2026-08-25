package com.aegispay.ingestion.api.dto;

public class TransactionRejectedException extends RuntimeException {
    public TransactionRejectedException(String reason) {
        super(reason);
    }
}
