package com.university.coreservice.infrastructure.primary.restful;

import com.university.coreservice.domain.model.PaymentRequestMessage;
import com.university.coreservice.domain.port.primary.CorePaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Driving Adapter (Primary Infrastructure) REST Controller tiếp nhận request từ PaymentGateway.
 * Gọi Primary Port CorePaymentProcessingService để xử lý.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentProcessController {

    private final CorePaymentProcessingService processingService;

    @PostMapping("/process")
    public ResponseEntity<Void> processPayment(@RequestBody PaymentRequestMessage request) {
        log.info("Received payment process request from PaymentGateway: referenceCode={}, type={}",
                request.getReferenceCode(), request.getTransactionType());
        processingService.processPayment(request);
        return ResponseEntity.accepted().build();
    }
}
