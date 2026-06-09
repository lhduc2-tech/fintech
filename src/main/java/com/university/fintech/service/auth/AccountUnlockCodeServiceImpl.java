package com.university.fintech.service.auth;

import com.university.fintech.domain.port.primary.AccountUnlockCodeService;
import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AccountUnlockCodeServiceImpl implements AccountUnlockCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisRestTokenStore tokenStore;

    @Value(Constant.ACCOUNT_UNLOCK_CODE_TTL_SECONDS_PROPERTY)
    private long unlockCodeTtlSeconds;

    @Override
    public String createCode(String phoneNumber) {
        String code = String.valueOf(SECURE_RANDOM.nextInt(
                Constant.ACCOUNT_UNLOCK_CODE_ORIGIN,
                Constant.ACCOUNT_UNLOCK_CODE_ORIGIN + Constant.ACCOUNT_UNLOCK_CODE_BOUND
        ));
        tokenStore.set(buildKey(phoneNumber), code, unlockCodeTtlSeconds);
        return code;
    }

    @Override
    public boolean isCodeValid(String phoneNumber, String code) {
        String storedCode = tokenStore.get(buildKey(phoneNumber));
        return storedCode != null && storedCode.equals(code);
    }

    @Override
    public void consumeCode(String phoneNumber) {
        tokenStore.delete(buildKey(phoneNumber));
    }

    private String buildKey(String phoneNumber) {
        return Constant.ACCOUNT_UNLOCK_REDIS_PREFIX + phoneNumber;
    }
}
