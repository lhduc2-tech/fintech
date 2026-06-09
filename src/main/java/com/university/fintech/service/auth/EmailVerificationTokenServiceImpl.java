package com.university.fintech.service.auth;

import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.domain.port.primary.EmailVerificationTokenService;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenServiceImpl implements EmailVerificationTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisRestTokenStore tokenStore;

    @Value(Constant.EMAIL_VERIFICATION_TTL_SECONDS_PROPERTY)
    private long verificationTtlSeconds;

    @Override
    public String createCode(String email) {
        String verificationCode = String.valueOf(SECURE_RANDOM.nextInt(
                Constant.VERIFICATION_CODE_ORIGIN,
                Constant.VERIFICATION_CODE_ORIGIN + Constant.VERIFICATION_CODE_BOUND
        ));
        tokenStore.set(buildKey(email), verificationCode, verificationTtlSeconds);
        return verificationCode;
    }

    @Override
    public boolean isCodeValid(String email, String verificationCode) {
        String storedCode = tokenStore.get(buildKey(email));
        return storedCode != null && storedCode.equals(verificationCode);
    }

    @Override
    public void consumeCode(String email) {
        tokenStore.delete(buildKey(email));
    }

    private String buildKey(String email) {
        return Constant.EMAIL_VERIFY_REDIS_PREFIX + email;
    }
}
