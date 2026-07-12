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
public class BankDTO {

    private UUID id;

    private String bankCode;

    private String bankName;

    private String accountNumber;

    private String accountHolderName;

    private String status;

    private BigDecimal balance;

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}
