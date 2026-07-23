package com.university.paymentgateway.domain.port.primary;

import com.university.paymentgateway.domain.model.PaymentRequestMessage;

/**
 * Primary (driving) port cho PaymentGateway.
 * Tiếp nhận yêu cầu chuyển tiếp từ Kafka Consumer.
 */
public interface PaymentGatewayForwardService {

    void forwardPaymentRequest(PaymentRequestMessage request);
}
