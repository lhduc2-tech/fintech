package com.university.fintech.infrastructure.primary.restful.request.wallet;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
    @DecimalMin(value = "0", inclusive = false, message = Constant.MESSAGE_WALLET_TRANSACTION_AMOUNT_POSITIVE)
    @DecimalMax(value = Constant.WALLET_TRANSACTION_MAX_AMOUNT_PER_TRANSACTION, message = Constant.MESSAGE_WALLET_TRANSACTION_AMOUNT_MAX_PER_TRANSACTION)
    @Digits(integer = 8, fraction = 0, message = Constant.MESSAGE_WALLET_TRANSACTION_AMOUNT_INTEGER)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 100)
    private String idempotencyKey;

    /** Mã PIN (6 chữ số) — bắt buộc nếu user đã tạo PIN. */
    @Size(min = 6, max = 6)
    private String pin;

    /** Mã OTP qua email — bắt buộc nếu user chưa tạo PIN. */
    @Size(min = 6, max = 6)
    private String otpCode;

    /**
     * FaceID token (UUID, TTL 5 phút) — bắt buộc khi {@code amount} ≥ 10,000,000 VNĐ.
     * Lấy từ {@code POST /wallet/faceid/verify} trước khi gọi API này.
     * Token chỉ dùng được <b>một lần</b>.
     */
    private String faceIdToken;
}
