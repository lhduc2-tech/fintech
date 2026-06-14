package com.university.fintech.infrastructure.primary.restful.request.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkBankAccountRequest {

    @NotBlank
    @Size(max = 50)
    private String bankCode;

    @NotBlank
    @Size(max = 50)
    private String accountNumber;
}
