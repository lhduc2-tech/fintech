package com.university.fintech.service.auth;

import com.university.fintech.domain.port.primary.WalletPinOtpService;
import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class WalletPinOtpServiceImpl implements WalletPinOtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisRestTokenStore tokenStore;

    @Value(Constant.WALLET_PIN_OTP_TTL_SECONDS_PROPERTY)
    private long otpTtlSeconds;

    @Override
    public String createOtp(String userId) {
        String otp = String.valueOf(SECURE_RANDOM.nextInt(
                Constant.WALLET_OTP_CODE_ORIGIN,
                Constant.WALLET_OTP_CODE_ORIGIN + Constant.WALLET_OTP_CODE_BOUND
        ));
        tokenStore.set(buildKey(userId), otp, otpTtlSeconds);
        return otp;
    }

    @Override
    public boolean isOtpValid(String userId, String otp) {
        String storedOtp = tokenStore.get(buildKey(userId));
        return storedOtp != null && storedOtp.equals(otp);
    }

    @Override
    public void consumeOtp(String userId) {
        tokenStore.delete(buildKey(userId));
    }

    private String buildKey(String userId) {
        return Constant.WALLET_PIN_OTP_REDIS_PREFIX + userId;
    }
}
