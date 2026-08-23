package com.university.fintech.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity ánh xạ tới bảng fraud_alerts.
 * Bảng này được ghi bởi Fraud Detection Python AI Engine,
 * Spring Boot chỉ đọc và cập nhật trường status.
 */
@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
public class FraudAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "reference_code", nullable = false, length = 100)
    private String referenceCode;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_full_name", length = 255)
    private String userFullName;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "recipient_full_name", length = 255)
    private String recipientFullName;

    @Column(name = "recipient_phone_number", length = 20)
    private String recipientPhoneNumber;

    @Column(name = "final_risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal finalRiskScore;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "system_action", nullable = false, columnDefinition = "text")
    private String systemAction;

    @Column(name = "rule_score", precision = 5, scale = 2)
    private BigDecimal ruleScore;

    @Column(name = "ai_score", precision = 5, scale = 2)
    private BigDecimal aiScore;

    @Column(name = "lgbm_prob_pct", precision = 5, scale = 2)
    private BigDecimal lgbmProbPct;

    @Column(name = "ae_reconstruction_error", precision = 10, scale = 6)
    private BigDecimal aeReconstructionError;

    @Column(name = "ae_threshold", precision = 10, scale = 6)
    private BigDecimal aeThreshold;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "triggered_rules", columnDefinition = "jsonb")
    private String triggeredRules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "threat_warnings", columnDefinition = "jsonb")
    private String threatWarnings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_snapshot", columnDefinition = "jsonb")
    private String payloadSnapshot;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "NEW";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
