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
 * Response DTO tóm tắt cho một fraud alert — dùng trong danh sách phân trang.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFraudAlertSummaryResponse {

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
    private BigDecimal finalRiskScore;
    private String riskLevel;
    private String systemAction;
    private BigDecimal ruleScore;
    private BigDecimal aiScore;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
