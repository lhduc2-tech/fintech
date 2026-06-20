package com.university.fintech.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionDTO {

    private UUID id;

    private UUID walletId;

    private UUID userId;

    private UUID linkedBankAccountId;

    private UUID counterpartyWalletId;

    private UUID counterpartyUserId;

    private String type;

    private BigDecimal amount;

    private String status;

    private String idempotencyKey;

    private String referenceCode;

    private String description;

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}
