package com.university.paymentgateway.infrastructure.kafka;

import com.university.paymentgateway.infrastructure.kafka.model.PaymentRequestMessage;
import com.university.paymentgateway.infrastructure.kafka.model.PaymentResultMessage;
import com.university.paymentgateway.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer nhận yêu cầu giao dịch từ fintech.
 * Decrypt → xử lý → encrypt kết quả → publish về payment.result.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final KafkaPayloadEncryptor encryptor;
    private final PaymentProcessingService processingService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${application.kafka.payment-result-topic}")
    private String paymentResultTopic;

    @KafkaListener(
            topics = "${application.kafka.payment-request-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String encryptedPayload, Acknowledgment acknowledgment) {
        PaymentRequestMessage request = null;
        try {
            request = encryptor.decrypt(encryptedPayload, PaymentRequestMessage.class);
            log.info("Received payment request: referenceCode={}, type={}, amount={}",
                    request.getReferenceCode(), request.getTransactionType(), request.getAmount());

            // Xử lý giao dịch
            PaymentResultMessage result = processingService.process(request);

            // Encrypt và publish kết quả về fintech
            String encryptedResult = encryptor.encrypt(result);
            kafkaTemplate.send(paymentResultTopic, result.getReferenceCode(), encryptedResult);

            log.info("Published payment result: referenceCode={}, status={}",
                    result.getReferenceCode(), result.getStatus());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            String ref = request != null ? request.getReferenceCode() : "unknown";
            log.error("Failed to process payment request: referenceCode={}, error={}", ref, e.getMessage(), e);
            // Không acknowledge → Kafka retry (PaymentProcessingService có idempotency guard)
            throw new RuntimeException("Failed to process payment request: " + ref, e);
        }
    }
}
