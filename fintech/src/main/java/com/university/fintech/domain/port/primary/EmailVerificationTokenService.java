package com.university.fintech.domain.port.primary;

public interface EmailVerificationTokenService {

    String createCode(String email);

    boolean isCodeValid(String email, String verificationCode);

    void consumeCode(String email);
}
