package com.university.fintech.domain.port.secondary.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class WalletProcessingRequest {

    private UUID userId;

    private String transactionType;

    private BigDecimal amount;

    private String idempotencyKey;

    private String referenceCode;
}
