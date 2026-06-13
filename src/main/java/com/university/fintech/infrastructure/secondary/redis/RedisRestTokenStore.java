package com.university.fintech.infrastructure.secondary.redis;

import com.university.fintech.exception.domain.AuthException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RedisRestTokenStore {

    private final RestClient restClient;
    private final boolean enabled;

    public RedisRestTokenStore(
            RestClient.Builder restClientBuilder,
            @Value("${spring.data.redis.url:}") String redisRestUrl,
            @Value("${spring.data.redis.token:}") String redisRestToken
    ) {
        this.enabled = redisRestUrl != null && !redisRestUrl.isBlank()
                && redisRestToken != null && !redisRestToken.isBlank();
        this.restClient = enabled
                ? restClientBuilder
                .baseUrl(redisRestUrl)
                .defaultHeader("Authorization", "Bearer " + redisRestToken)
                .build()
                : null;
    }

    public void set(String key, String value, long ttlSeconds) {
        requireEnabled();
        restClient.post()
                .body(List.of("SET", key, value, "EX", String.valueOf(ttlSeconds)))
                .retrieve()
                .body(RedisRestResponse.class);
    }

    public String get(String key) {
        if (!enabled) {
            return null;
        }
        RedisRestResponse response = restClient.post()
                .body(List.of("GET", key))
                .retrieve()
                .body(RedisRestResponse.class);
        return response == null || response.getResult() == null ? null : response.getResult().toString();
    }

    public long ttl(String key) {
        if (!enabled) {
            return -2;
        }
        RedisRestResponse response = restClient.post()
                .body(List.of("TTL", key))
                .retrieve()
                .body(RedisRestResponse.class);
        if (response == null || response.getResult() == null) {
            return -2;
        }
        return Long.parseLong(response.getResult().toString());
    }

    public void delete(String key) {
        requireEnabled();
        restClient.post()
                .body(List.of("DEL", key))
                .retrieve()
                .body(RedisRestResponse.class);
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new AuthException("Redis REST configuration is missing.", "REDIS_NOT_CONFIGURED");
        }
    }

    @Getter
    @Setter
    private static class RedisRestResponse {
        private Object result;
    }
}
