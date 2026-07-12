package com.university.fintech.infrastructure.primary.restful.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {

    private UUID id;

    private String bankCode;

    private String bankName;

    private String accountNumber;

    private String accountHolderName;

    private String status;
}
