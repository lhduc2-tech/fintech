package com.university.paymentgateway.infrastructure.primary.kafka;

import com.university.paymentgateway.domain.model.PaymentRequestMessage;
import com.university.paymentgateway.domain.port.primary.PaymentGatewayForwardService;
import com.university.paymentgateway.infrastructure.secondary.kafka.KafkaPayloadEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Driving Adapter (Primary Infrastructure) consume Kafka message từ fintech.
 * Decrypt payload và gọi Primary Port PaymentGatewayForwardService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final KafkaPayloadEncryptor encryptor;
    private final PaymentGatewayForwardService forwardService;

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

            forwardService.forwardPaymentRequest(request);

            acknowledgment.acknowledge();
            log.info("Forwarded payment request to CoreService: referenceCode={}", request.getReferenceCode());

        } catch (Exception e) {
            String ref = request != null ? request.getReferenceCode() : "unknown";
            log.error("Failed to forward payment request: referenceCode={}, error={}", ref, e.getMessage(), e);
            throw new RuntimeException("Failed to forward payment request: " + ref, e);
        }
    }
}
