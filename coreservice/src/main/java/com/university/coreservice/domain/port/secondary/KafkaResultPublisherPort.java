package com.university.coreservice.domain.port.secondary;

import com.university.coreservice.domain.model.MailNotificationMessage;
import com.university.coreservice.domain.model.PaymentResultMessage;

/**
 * Secondary (driven) port publish kết quả giao dịch lên Kafka.
 */
public interface KafkaResultPublisherPort {

    void publishPaymentResult(PaymentResultMessage result);

    void publishMailNotification(MailNotificationMessage mail);
}
