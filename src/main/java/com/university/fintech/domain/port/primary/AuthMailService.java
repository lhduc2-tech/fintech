package com.university.fintech.domain.port.primary;

public interface AuthMailService {

    void sendVerificationCode(String to, String fullName, String verificationCode);

    void sendAccountUnlockCode(String to, String fullName, String unlockCode);

    void sendPasswordResetCode(String to, String fullName, String resetCode);
}
