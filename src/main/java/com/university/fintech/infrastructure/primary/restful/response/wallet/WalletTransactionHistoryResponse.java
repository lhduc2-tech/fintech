package com.university.fintech.infrastructure.primary.restful.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionHistoryResponse {

    private BigDecimal amount;

    private String type;

    private String status;

    private OffsetDateTime updatedAt;

    private String description;
}
