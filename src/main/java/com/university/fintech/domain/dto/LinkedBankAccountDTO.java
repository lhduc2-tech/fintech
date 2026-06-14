package com.university.fintech.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedBankAccountDTO {

    private UUID id;

    private UUID walletId;

    private UUID bankId;

    private String bankCode;

    private String bankName;

    private String accountNumber;

    private String accountHolderName;

    private String status;

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}
