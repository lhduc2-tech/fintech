package com.university.fintech.infrastructure.primary.restful.request.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LinkBankAccountRequest {

    @NotBlank
    @Size(max = 50)
    private String bankCode;

    @Size(max = 255)
    private String bankName;

    @NotBlank
    @Size(max = 50)
    private String accountNumber;

    @NotBlank
    @Size(max = 255)
    private String accountHolderName;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal balance;
}
