package com.university.fintech.service.auth;

import com.university.fintech.domain.port.primary.EmailVerificationRateLimitService;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailVerificationRateLimitServiceImpl implements EmailVerificationRateLimitService {

    private final RedisRestTokenStore tokenStore;

    @Value(Constant.EMAIL_VERIFICATION_SEND_MAX_ATTEMPTS_PROPERTY)
    private int maxAttempts;

    @Value(Constant.EMAIL_VERIFICATION_SEND_WINDOW_SECONDS_PROPERTY)
    private long windowSeconds;

    @Value(Constant.EMAIL_VERIFICATION_SEND_COOLDOWN_SECONDS_PROPERTY)
    private long cooldownSeconds;

    @Override
    public void assertCanSend(String email) {
        String cooldownKey = buildCooldownKey(email);
        if (tokenStore.get(cooldownKey) != null) {
            throw new AuthException(
                    Constant.MESSAGE_VERIFICATION_CODE_SEND_COOLDOWN,
                    Constant.ERROR_VERIFICATION_CODE_RATE_LIMITED,
                    Map.of("retryAfterSeconds", getPositiveTtl(cooldownKey, cooldownSeconds))
            );
        }

        int sentCount = parseCount(tokenStore.get(buildCountKey(email)));
        if (sentCount >= maxAttempts) {
            throw new AuthException(
                    Constant.MESSAGE_VERIFICATION_CODE_SEND_LIMIT_EXCEEDED,
                    Constant.ERROR_VERIFICATION_CODE_RATE_LIMITED,
                    Map.of(
                            "maxAttempts", maxAttempts,
                            "windowSeconds", windowSeconds
                    )
            );
        }
    }

    @Override
    public void recordSent(String email) {
        int sentCount = parseCount(tokenStore.get(buildCountKey(email))) + 1;
        tokenStore.set(buildCountKey(email), String.valueOf(sentCount), windowSeconds);
        tokenStore.set(buildCooldownKey(email), Constant.TOKEN_BLACKLIST_VALUE, cooldownSeconds);
    }

    private String buildCountKey(String email) {
        return Constant.EMAIL_VERIFY_SEND_COUNT_REDIS_PREFIX + email;
    }

    private String buildCooldownKey(String email) {
        return Constant.EMAIL_VERIFY_SEND_COOLDOWN_REDIS_PREFIX + email;
    }

    private int parseCount(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long getPositiveTtl(String key, long fallbackSeconds) {
        long ttl = tokenStore.ttl(key);
        return ttl > 0 ? ttl : fallbackSeconds;
    }
}
