package com.university.notificationservice.domain.port.primary;

import com.university.notificationservice.domain.model.MailNotificationMessage;

/**
 * Primary (driving) port cho NotificationService.
 * Phụ trách nhận yêu cầu gửi mail giao dịch từ Kafka Consumer.
 */
public interface MailNotificationService {

    void sendTransactionNotification(MailNotificationMessage message);
}
