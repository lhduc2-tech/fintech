package com.university.fintech.infrastructure.secondary.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultMessage {

    /** Khớp với correlationId trong PaymentRequestMessage */
    private String correlationId;

    /** referenceCode từ WalletTransaction - dùng để tìm transaction cần update */
    private String referenceCode;

    /** BANK_TO_WALLET / WALLET_TO_BANK / WALLET_TRANSFER_OUT */
    private String transactionType;

    /** SUCCESS hoặc FAILED */
    private String status;

    /** Lý do thất bại (null nếu SUCCESS) */
    private String failureReason;

    private String timestamp;
}
