package com.university.notificationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailNotificationMessage {

    private String to;
    private String fullName;
    private String transactionType;
    private BigDecimal amount;
    private String status;
    private String referenceCode;
    private String description;
    private String failureReason;
    private String recipientFullName;
    private String recipientPhoneNumber;
}
