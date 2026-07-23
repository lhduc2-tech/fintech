package com.university.fintech.infrastructure.secondary.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestMessage {

    /** UUID dùng để track request này trong PaymentGateway */
    private String correlationId;

    /** referenceCode từ WalletTransaction trong fintech */
    private String referenceCode;

    /** BANK_TO_WALLET / WALLET_TO_BANK / WALLET_TRANSFER_OUT */
    private String transactionType;

    private BigDecimal amount;

    /** Dùng để idempotency check ở PaymentGateway */
    private String idempotencyKey;

    /** Mã ngân hàng (null với WALLET_TRANSFER_OUT) */
    private String bankCode;

    /** Số tài khoản ngân hàng (null với WALLET_TRANSFER_OUT) */
    private String accountNumber;

    private String timestamp;

    // ── Thông tin người dùng để CoreService publish mail notification ─────────

    /** Email người thực hiện giao dịch */
    private String userEmail;

    /** Tên đầy đủ người thực hiện giao dịch */
    private String userFullName;

    /** Mô tả giao dịch (VD: "Nạp tiền từ VCB 1234") */
    private String description;

    /** Tên đầy đủ người nhận (null nếu không phải WALLET_TRANSFER_OUT) */
    private String recipientFullName;

    /** Số điện thoại người nhận (null nếu không phải WALLET_TRANSFER_OUT) */
    private String recipientPhoneNumber;
}

