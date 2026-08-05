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

    // ── Các trường dữ liệu phục vụ Mô hình AI / GraphSAGE (Tier 3 GNN) & Anti-Fraud ──

    /** 1. Định danh nguồn gửi */
    private String senderUserId;
    private String senderAccountNumber;

    /** 2. Số dư trước & sau giao dịch */
    private BigDecimal oldBalance;
    private BigDecimal newBalance;

    /** 3. Mã định danh thiết bị */
    private String deviceId;
    private String deviceFingerprint;

    /** 4. Địa chỉ IP & Vị trí địa lý */
    private String clientIp;
    private String geoLocation;

    /** 5. Lịch sử giao dịch 24h của ví (Velocity Checks) */
    private Integer velocity24hCount;
    private BigDecimal velocity24hAmount;

    /** 6. Số lượng đối tác gửi/nhận 24h (Fan-In / Fan-Out Checks) */
    private Integer fanIn24hCount;
    private Integer fanOut24hCount;
}

