package com.university.fintech.infrastructure.primary.restful.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedBankAccountResponse {

    private UUID id;

    private String bankCode;

    private String bankName;

    private String accountNumber;

    private String accountHolderName;

    private BigDecimal balance;

    private String status;
}
