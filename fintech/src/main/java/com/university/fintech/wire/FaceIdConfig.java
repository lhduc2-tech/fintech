package com.university.fintech.wire;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Cấu hình cho FaceID Python server integration.
 * Đọc từ {@code application.faceid.*} trong application.yaml.
 */
@Configuration
@ConfigurationProperties(prefix = "application.faceid")
@Getter
@Setter
public class FaceIdConfig {

    /** URL của FaceID Python server (nội bộ Docker, ví dụ: http://faceid-server:5001) */
    private String serverUrl = "http://localhost:5001";


    /** Ngưỡng giá trị giao dịch bắt buộc FaceID (VNĐ), mặc định 10,000,000 */
    private BigDecimal highValueThreshold = new BigDecimal("10000000");

    /**
     * Bật/tắt FaceID. Khi {@code false}, bỏ qua mọi kiểm tra FaceID
     * (chỉ dùng khi dev/test local không có Python server).
     */
    private boolean enabled = true;

    /**
     * RestTemplate riêng cho FaceID client với timeout hợp lý.
     * Timeout 15s — AI inference có thể chậm hơn REST thông thường.
     */
    @Bean("faceIdRestTemplate")
    public RestTemplate faceIdRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(90))
                .build();
    }
}

