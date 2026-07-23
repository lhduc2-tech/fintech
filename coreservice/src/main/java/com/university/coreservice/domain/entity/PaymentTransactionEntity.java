package com.university.coreservice.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.university.coreservice.domain.audit.BaseAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lưu audit trail của mọi giao dịch CoreService đã xử lý.
 * Bảng `payment_transactions` trong DB fintech.
 * Đóng vai trò idempotency guard: chống xử lý trùng khi PG retry.
 */
@Entity
@Table(name = "payment_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_tx_idempotency", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uk_payment_tx_correlation", columnNames = "correlation_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionEntity extends BaseAuditing {

    @Id
    private UUID id;

    /** ID để track request từ fintech qua PaymentGateway */
    @Column(name = "correlation_id", nullable = false, unique = true)
    private String correlationId;

    /** referenceCode từ WalletTransaction của fintech */
    @Column(name = "reference_code", nullable = false)
    private String referenceCode;

    /** BANK_TO_WALLET / WALLET_TO_BANK / WALLET_TRANSFER_OUT */
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /** SUCCESS / FAILED */
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "failure_reason")
    private String failureReason;

    /** Dùng để idempotency check - tránh xử lý trùng kể cả khi Kafka retry */
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
