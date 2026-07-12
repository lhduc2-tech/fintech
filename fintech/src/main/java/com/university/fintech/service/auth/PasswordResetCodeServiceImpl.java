package com.university.fintech.service.auth;

import com.university.fintech.domain.port.primary.PasswordResetCodeService;
import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PasswordResetCodeServiceImpl implements PasswordResetCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisRestTokenStore tokenStore;

    @Value(Constant.PASSWORD_RESET_CODE_TTL_SECONDS_PROPERTY)
    private long resetCodeTtlSeconds;

    @Override
    public String createCode(String email) {
        String code = String.valueOf(SECURE_RANDOM.nextInt(
                Constant.PASSWORD_RESET_CODE_ORIGIN,
                Constant.PASSWORD_RESET_CODE_ORIGIN + Constant.PASSWORD_RESET_CODE_BOUND
        ));
        tokenStore.set(buildKey(email), code, resetCodeTtlSeconds);
        return code;
    }

    @Override
    public boolean isCodeValid(String email, String code) {
        String storedCode = tokenStore.get(buildKey(email));
        return storedCode != null && storedCode.equals(code);
    }

    @Override
    public void consumeCode(String email) {
        tokenStore.delete(buildKey(email));
    }

    private String buildKey(String email) {
        return Constant.PASSWORD_RESET_REDIS_PREFIX + email;
    }
}
