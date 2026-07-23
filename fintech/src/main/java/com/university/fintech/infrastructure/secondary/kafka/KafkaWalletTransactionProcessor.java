package com.university.fintech.infrastructure.secondary.kafka;

import com.university.fintech.domain.port.secondary.WalletTransactionProcessor;
import com.university.fintech.domain.port.secondary.model.WalletProcessingRequest;
import com.university.fintech.domain.port.secondary.model.WalletProcessingResult;
import com.university.fintech.infrastructure.secondary.kafka.model.PaymentRequestMessage;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Kafka-based WalletTransactionProcessor.
 * Publish yêu cầu xử lý giao dịch lên Kafka (sau khi DB transaction commit)
 * và luôn trả về PENDING ngay lập tức (async pattern).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaWalletTransactionProcessor implements WalletTransactionProcessor {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaPayloadEncryptor encryptor;

    @Value("${application.kafka.payment-request-topic}")
    private String paymentRequestTopic;

    @Override
    public WalletProcessingResult process(WalletProcessingRequest request) {
        PaymentRequestMessage message = PaymentRequestMessage.builder()
                .correlationId(UUID.randomUUID().toString())
                .referenceCode(request.getReferenceCode())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .idempotencyKey(request.getIdempotencyKey())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .userEmail(request.getUserEmail())
                .userFullName(request.getUserFullName())
                .description(request.getDescription())
                .recipientFullName(request.getRecipientFullName())
                .recipientPhoneNumber(request.getRecipientPhoneNumber())
                .timestamp(OffsetDateTime.now().toString())
                .build();

        // Publish SAU KHI DB transaction commit để tránh race condition
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishMessage(message);
                }
            });
        } else {
            publishMessage(message);
        }

        // Luôn trả về PENDING - fintech consumer sẽ update sau khi nhận kết quả từ PG
        return WalletProcessingResult.builder()
                .status(Constant.STATUS_PENDING)
                .message("Transaction submitted to payment gateway.")
                .build();
    }

    private void publishMessage(PaymentRequestMessage message) {
        try {
            String encryptedPayload = encryptor.encrypt(message);

            log.debug("[ENCRYPTION CHECK] Payload sent to Kafka topic '{}': {}",
                    paymentRequestTopic, encryptedPayload);
            kafkaTemplate.send(paymentRequestTopic, message.getReferenceCode(), encryptedPayload);
            log.info("Published payment request to Kafka: referenceCode={}, type={}",
                    message.getReferenceCode(), message.getTransactionType());
        } catch (Exception e) {
            log.error("Failed to publish payment request to Kafka: referenceCode={}",
                    message.getReferenceCode(), e);
            throw new RuntimeException("Failed to submit transaction to payment gateway", e);
        }
    }
}

