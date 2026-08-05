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
