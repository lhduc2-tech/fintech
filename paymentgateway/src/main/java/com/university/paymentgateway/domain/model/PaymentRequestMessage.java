package com.university.paymentgateway.domain.model;

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

    private String correlationId;
    private String referenceCode;
    private String transactionType;
    private BigDecimal amount;
    private String idempotencyKey;
    private String bankCode;
    private String accountNumber;
    private String timestamp;

    private String userEmail;
    private String userFullName;
    private String description;
    private String recipientFullName;
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
