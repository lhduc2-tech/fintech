package com.university.fintech.domain.port.primary;

public interface WalletPinOtpService {

    String createOtp(String userId);

    boolean isOtpValid(String userId, String otp);

    void consumeOtp(String userId);
}
