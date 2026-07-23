package com.university.coreservice.domain.port.primary;

import com.university.coreservice.domain.model.PaymentRequestMessage;

/**
 * Primary (driving) port cho CoreService.
 * Tiếp nhận yêu cầu xử lý giao dịch từ REST Controller.
 */
public interface CorePaymentProcessingService {

    void processPayment(PaymentRequestMessage request);
}
