package com.university.coreservice.infrastructure.secondary.kafka;

import com.university.coreservice.domain.model.MailNotificationMessage;
import com.university.coreservice.domain.model.PaymentResultMessage;
import com.university.coreservice.domain.port.secondary.KafkaResultPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Driven Adapter (Secondary Infrastructure) publish kết quả giao dịch lên Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaResultPublisherAdapter implements KafkaResultPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaPayloadEncryptor encryptor;

    @Value("${application.kafka.payment-result-topic}")
    private String paymentResultTopic;

    @Value("${application.kafka.payment-mail-topic}")
    private String paymentMailTopic;

    @Override
    public void publishPaymentResult(PaymentResultMessage result) {
        try {
            String encrypted = encryptor.encrypt(result);
            kafkaTemplate.send(paymentResultTopic, result.getReferenceCode(), encrypted);
            log.info("Published payment.result: referenceCode={}, status={}",
                    result.getReferenceCode(), result.getStatus());
        } catch (Exception e) {
            log.error("Failed to publish payment.result: referenceCode={}", result.getReferenceCode(), e);
            throw new RuntimeException("Failed to publish payment result", e);
        }
    }

    @Override
    public void publishMailNotification(MailNotificationMessage mail) {
        if (mail.getTo() == null || mail.getTo().isBlank()) {
            log.warn("Skipping mail notification: no email address for referenceCode={}", mail.getReferenceCode());
            return;
        }
        try {
            String encrypted = encryptor.encrypt(mail);
            kafkaTemplate.send(paymentMailTopic, mail.getReferenceCode(), encrypted);
            log.info("Published payment.mail: to={}, referenceCode={}, status={}",
                    mail.getTo(), mail.getReferenceCode(), mail.getStatus());
        } catch (Exception e) {
            log.error("Failed to publish payment.mail: referenceCode={}", mail.getReferenceCode(), e);
            log.warn("Mail notification skipped due to publish failure: referenceCode={}", mail.getReferenceCode());
        }
    }
}
