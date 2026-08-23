package com.university.fintech.infrastructure.primary.restful.response.admin.fraudalert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO đầy đủ cho một fraud alert — dùng trong modal chi tiết.
 * Bao gồm triggered_rules, threat_warnings, payload_snapshot dưới dạng raw JSON string.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFraudAlertDetailResponse {

    private UUID id;
    private String correlationId;
    private String referenceCode;
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private String transactionType;
    private BigDecimal amount;
    private String bankCode;
    private String accountNumber;
    private String recipientFullName;
    private String recipientPhoneNumber;
    private BigDecimal finalRiskScore;
    private String riskLevel;
    private String systemAction;
    private BigDecimal ruleScore;
    private BigDecimal aiScore;
    private BigDecimal lgbmProbPct;
    private BigDecimal aeReconstructionError;
    private BigDecimal aeThreshold;

    /** Raw JSON array string của danh sách rule bị trigger */
    private String triggeredRules;

    /** Raw JSON array string của danh sách cảnh báo nguy cơ */
    private String threatWarnings;

    /** Raw JSON string của payload giao dịch gốc */
    private String payloadSnapshot;

    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
