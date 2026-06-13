package com.university.fintech.infrastructure.primary.restful.request.wallet;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WalletTransferRequest {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = Constant.PHONE_NUMBER_NUMERIC_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_NUMERIC_ONLY)
    @Pattern(regexp = Constant.PHONE_NUMBER_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS)
    private String recipientPhoneNumber;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    @Size(max = 100)
    private String idempotencyKey;
}
