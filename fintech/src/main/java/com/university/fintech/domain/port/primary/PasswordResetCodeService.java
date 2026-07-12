package com.university.fintech.domain.port.primary;

public interface PasswordResetCodeService {

    String createCode(String email);

    boolean isCodeValid(String email, String code);

    void consumeCode(String email);
}
