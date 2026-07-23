package com.university.paymentgateway.domain.port.secondary;

import com.university.paymentgateway.domain.model.PaymentRequestMessage;

/**
 * Secondary (driven) port cho PaymentGateway.
 * Gửi HTTP request tới CoreService.
 */
public interface CoreServiceClientPort {

    void sendToCoreService(PaymentRequestMessage request);
}
