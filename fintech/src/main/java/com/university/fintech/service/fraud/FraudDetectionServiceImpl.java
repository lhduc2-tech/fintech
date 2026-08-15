package com.university.fintech.service.fraud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.university.fintech.domain.dto.FraudDetectionPayload;
import com.university.fintech.domain.port.primary.FraudDetectionService;
import com.university.fintech.wire.FraudDetectionConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Triển khai FraudDetectionService — giao tiếp với Fraud Detection AI Python microservice.
 * Mọi yêu cầu được gửi hoàn toàn bất đồng bộ (Non-blocking), bảo đảm 100% không ảnh hưởng
 * tới luồng xử lý giao dịch chính qua PaymentGateway -> CoreService -> NotificationService.
 */
@Service
@Slf4j
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudDetectionConfig fraudDetectionConfig;
    private final RestTemplate fraudDetectionRestTemplate;

    public FraudDetectionServiceImpl(
            FraudDetectionConfig fraudDetectionConfig,
            @Qualifier("fraudDetectionRestTemplate") RestTemplate fraudDetectionRestTemplate
    ) {
        this.fraudDetectionConfig = fraudDetectionConfig;
        this.fraudDetectionRestTemplate = fraudDetectionRestTemplate;
    }

    @Override
    public void screenTransactionAsync(FraudDetectionPayload payload) {
        if (!fraudDetectionConfig.isEnabled()) {
            log.debug("[FraudDetection] Service disabled — bỏ qua sàng lọc cho referenceCode={}", payload.getReferenceCode());
            return;
        }

        // Chạy bất đồng bộ trong background thread pool (Virtual Threads)
        CompletableFuture.runAsync(() -> {
            try {
                String url = fraudDetectionConfig.getServerUrl() + "/api/v1/fraud/screen";
                log.info("[FraudDetection] Gửi giao dịch sang Fraud Detection Service: referenceCode={}, type={}, amount={}",
                        payload.getReferenceCode(), payload.getTransactionType(), payload.getAmount());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<FraudDetectionPayload> entity = new HttpEntity<>(payload, headers);

                FraudScreenResponse response = fraudDetectionRestTemplate.postForObject(url, entity, FraudScreenResponse.class);

                if (response != null && response.getData() != null) {
                    FraudResultData data = response.getData();
                    log.info("[FraudDetection] Kết quả sàng lọc referenceCode={}: RiskScore={}/100, Level=[{}], Action={}",
                            payload.getReferenceCode(), data.getFinalRiskScore(), data.getRiskLevel(), data.getSystemAction());
                } else {
                    log.warn("[FraudDetection] Nhận phản hồi rỗng từ Fraud Service cho referenceCode={}", payload.getReferenceCode());
                }
            } catch (Exception e) {
                // Bắt toàn bộ ngoại lệ để không bao giờ làm gián đoạn luồng chính
                log.warn("[FraudDetection] Không thể kết nối tới Fraud Detection Service (referenceCode={}): {}",
                        payload.getReferenceCode(), e.getMessage());
            }
        });
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FraudScreenResponse {
        private Boolean success;
        private String message;
        private FraudResultData data;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FraudResultData {
        private String correlationId;
        private String referenceCode;
        private Double finalRiskScore;
        private String riskLevel;
        private String systemAction;
        private Double ruleScore;
        private Double aiScore;
        private String alertId;
    }
}
