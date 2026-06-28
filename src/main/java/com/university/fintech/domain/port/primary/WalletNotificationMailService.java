package com.university.fintech.domain.port.primary;

import java.math.BigDecimal;

public interface WalletNotificationMailService {

    void sendTransactionNotification(
            String to,
            String fullName,
            String transactionType,
            BigDecimal amount,
            String status,
            String referenceCode,
            String description,
            String recipientFullName,
            String recipientPhoneNumber
    );

    void sendTransactionErrorNotification(
            String to,
            String fullName,
            String transactionType,
            BigDecimal amount,
            String errorMessage
    );
}
