package com.university.paymentgateway.infrastructure.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultMessage {
    private String correlationId;
    private String referenceCode;
    private String transactionType;
    private String status;
    private String failureReason;
    private String timestamp;
}
