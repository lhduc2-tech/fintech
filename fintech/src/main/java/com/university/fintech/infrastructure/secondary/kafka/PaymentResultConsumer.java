package com.university.fintech.infrastructure.secondary.kafka;

import com.university.fintech.infrastructure.secondary.kafka.model.PaymentResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer nhận kết quả xử lý giao dịch từ PaymentGateway.
 * Giải mã payload và chuyển sang WalletTransactionUpdater để cập nhật DB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final KafkaPayloadEncryptor encryptor;
    private final WalletTransactionUpdater updater;

    @KafkaListener(
            topics = "${application.kafka.payment-result-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String encryptedPayload, Acknowledgment acknowledgment) {
        try {
            PaymentResultMessage result = encryptor.decrypt(encryptedPayload, PaymentResultMessage.class);
            log.info("Received payment result: referenceCode={}, type={}, status={}",
                    result.getReferenceCode(), result.getTransactionType(), result.getStatus());
            updater.handlePaymentResult(result);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process payment result message: {}", e.getMessage(), e);
            // Không acknowledge → Kafka retry (idempotency trong WalletTransactionUpdater đảm bảo an toàn)
            throw new RuntimeException("Failed to process payment result", e);
        }
    }
}
