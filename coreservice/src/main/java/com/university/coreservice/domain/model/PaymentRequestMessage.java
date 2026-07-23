package com.university.coreservice.domain.model;

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
}
