package com.university.paymentgateway.wire.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * WebConfig cho PaymentGateway.
 * Sử dụng JDK 11+ HttpClient với Connection Pooling tự động,
 * Connect Timeout (2s) và Read Timeout (5s) để giảm tối đa HTTP Latency.
 */
@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return new RestTemplate(requestFactory);
    }
}
