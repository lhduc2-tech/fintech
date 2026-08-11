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
public class WalletDTO {

    private UUID id;

    private UUID userId;

    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal holdBalance = BigDecimal.ZERO;

    private String status;

    private Integer limitation;

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}
