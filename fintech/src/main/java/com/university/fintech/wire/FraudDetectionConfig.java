package com.university.fintech.wire;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Cấu hình cho Fraud Detection Python AI server integration.
 * Đọc từ {@code application.fraud-detection.*} trong application.yaml.
 */
@Configuration
@ConfigurationProperties(prefix = "application.fraud-detection")
@Getter
@Setter
public class FraudDetectionConfig {

    /** URL của Fraud Detection Python server (ví dụ: http://localhost:5002 hoặc http://fraud-detection-service:5002) */
    private String serverUrl = "http://localhost:5002";

    /** Bật/tắt gọi Fraud Detection. Khi false, bỏ qua sàng lọc */
    private boolean enabled = true;

    /** Thời gian timeout (giây) khi gọi REST tới Fraud Detection server */
    private int timeoutSeconds = 5;

    /**
     * RestTemplate riêng cho Fraud Detection client với timeout ngắn để hoàn toàn non-blocking.
     */
    @Bean("fraudDetectionRestTemplate")
    public RestTemplate fraudDetectionRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(timeoutSeconds > 0 ? timeoutSeconds : 5))
                .build();
    }
}
