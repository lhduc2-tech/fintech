package com.university.fintech.infrastructure.primary.restful.response.wallet;

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
public class WalletTransactionResponse {

    private UUID id;

    private UUID walletId;

    private UUID linkedBankAccountId;

    private UUID counterpartyWalletId;

    private UUID counterpartyUserId;

    private String type;

    private BigDecimal amount;

    private String status;

    private String idempotencyKey;

    private String referenceCode;

    private OffsetDateTime createdAt;
}
