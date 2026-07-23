package com.university.paymentgateway.service;

import com.university.paymentgateway.domain.model.PaymentRequestMessage;
import com.university.paymentgateway.domain.port.primary.PaymentGatewayForwardService;
import com.university.paymentgateway.domain.port.secondary.CoreServiceClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation cho Primary Port PaymentGatewayForwardService.
 * Chuyển tiếp request tới Secondary Port CoreServiceClientPort.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayForwardServiceImpl implements PaymentGatewayForwardService {

    private final CoreServiceClientPort coreServiceClientPort;

    @Override
    public void forwardPaymentRequest(PaymentRequestMessage request) {
        log.info("Processing forward payment request: referenceCode={}, type={}",
                request.getReferenceCode(), request.getTransactionType());
        coreServiceClientPort.sendToCoreService(request);
    }
}
