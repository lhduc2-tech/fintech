package com.university.notificationservice.infrastructure.primary.kafka;

import com.university.notificationservice.domain.model.MailNotificationMessage;
import com.university.notificationservice.domain.port.primary.MailNotificationService;
import com.university.notificationservice.infrastructure.secondary.kafka.KafkaPayloadEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Driving Adapter (Primary Infrastructure) consume Kafka message từ topic payment.mail.
 * Decrypt payload và gọi Primary Port MailNotificationService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailNotificationConsumer {

    private final KafkaPayloadEncryptor encryptor;
    private final MailNotificationService mailNotificationService;

    @KafkaListener(
            topics = "${application.kafka.payment-mail-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String encryptedPayload, Acknowledgment acknowledgment) {
        MailNotificationMessage message = null;
        try {
            message = encryptor.decrypt(encryptedPayload, MailNotificationMessage.class);
            log.info("Received mail notification: to={}, type={}, status={}",
                    message.getTo(), message.getTransactionType(), message.getStatus());

            mailNotificationService.sendTransactionNotification(message);

            acknowledgment.acknowledge();
            log.info("Mail notification processed successfully: to={}, referenceCode={}",
                    message.getTo(), message.getReferenceCode());

        } catch (Exception e) {
            String to = message != null ? message.getTo() : "unknown";
            log.error("Failed to process mail notification: to={}, error={}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to process mail notification", e);
        }
    }
}
