package com.university.fintech.domain.port.primary;

public interface EmailVerificationRateLimitService {

    void assertCanSend(String email);

    void recordSent(String email);
}
