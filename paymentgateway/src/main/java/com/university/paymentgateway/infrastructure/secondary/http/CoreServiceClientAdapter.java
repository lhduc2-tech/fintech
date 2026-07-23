package com.university.paymentgateway.infrastructure.secondary.http;

import com.university.paymentgateway.domain.model.PaymentRequestMessage;
import com.university.paymentgateway.domain.port.secondary.CoreServiceClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Driven Adapter (Secondary Infrastructure) gọi REST sang CoreService.
 */
@Slf4j
@Component
public class CoreServiceClientAdapter implements CoreServiceClientPort {

    private final RestTemplate restTemplate;
    private final String coreServiceUrl;

    public CoreServiceClientAdapter(
            RestTemplate restTemplate,
            @Value("${application.coreservice.url:http://localhost:8086}") String coreServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.coreServiceUrl = coreServiceUrl;
    }

    @Override
    public void sendToCoreService(PaymentRequestMessage request) {
        String url = coreServiceUrl + "/api/v1/payments/process";
        log.info("Forwarding payment request to CoreService: referenceCode={}, type={}",
                request.getReferenceCode(), request.getTransactionType());
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            log.info("CoreService accepted payment request: referenceCode={}, httpStatus={}",
                    request.getReferenceCode(), response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to forward payment request to CoreService: referenceCode={}, error={}",
                    request.getReferenceCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to forward payment request to CoreService: " + request.getReferenceCode(), e);
        }
    }
}
