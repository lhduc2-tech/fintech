package com.university.fintech.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO Payload gửi tới Fraud Detection Python Server để sàng lọc rủi ro.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionPayload {

    private String correlationId;
    private String referenceCode;
    private String idempotencyKey;
    private String transactionType;
    private BigDecimal amount;
    private String timestamp;

    private UUID userId;
    private String userEmail;
    private String userFullName;
    private String description;

    private String bankCode;
    private String accountNumber;
    private String recipientFullName;
    private String recipientPhoneNumber;

    private String senderUserId;
    private String senderAccountNumber;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;

    private String deviceId;
    private String deviceFingerprint;
    private String clientIp;
    private String geoLocation;

    private Integer velocity24hCount;
    private BigDecimal velocity24hAmount;
    private Integer fanIn24hCount;
    private Integer fanOut24hCount;
}
