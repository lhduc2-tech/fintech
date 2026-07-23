package com.university.fintech.domain.port.secondary.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class WalletProcessingRequest {

    private UUID userId;

    private String transactionType;

    private BigDecimal amount;

    private String idempotencyKey;

    private String referenceCode;

    /** Mã ngân hàng - null cho WALLET_TRANSFER_OUT */
    private String bankCode;

    /** Số tài khoản ngân hàng - null cho WALLET_TRANSFER_OUT */
    private String accountNumber;

    // ── Thông tin để gửi mail notification (qua NotificationService) ─────────

    /** Email của người thực hiện giao dịch */
    private String userEmail;

    /** Tên đầy đủ của người thực hiện giao dịch */
    private String userFullName;

    /** Mô tả giao dịch (VD: "Nạp tiền từ tài khoản VCB 1234") */
    private String description;

    /** Tên đầy đủ người nhận (null nếu không phải WALLET_TRANSFER_OUT) */
    private String recipientFullName;

    /** Số điện thoại người nhận (null nếu không phải WALLET_TRANSFER_OUT) */
    private String recipientPhoneNumber;
}
