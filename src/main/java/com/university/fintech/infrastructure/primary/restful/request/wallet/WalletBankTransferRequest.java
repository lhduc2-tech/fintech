package com.university.fintech.infrastructure.primary.restful.request.wallet;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class WalletBankTransferRequest {

    @NotNull
    private UUID linkedBankAccountId;

    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = Constant.MESSAGE_WALLET_TRANSACTION_AMOUNT_POSITIVE)
    @DecimalMax(value = Constant.WALLET_TRANSACTION_MAX_AMOUNT_PER_TRANSACTION, message = Constant.MESSAGE_WALLET_TRANSACTION_AMOUNT_MAX_PER_TRANSACTION)
    @Digits(integer = 8, fraction = 0, message = Constant.MESSAGE_WALLET_TRANSACTION_AMOUNT_INTEGER)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 100)
    private String idempotencyKey;
}
