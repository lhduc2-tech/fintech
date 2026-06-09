package com.university.fintech.domain.port.primary;

public interface AccountUnlockCodeService {

    String createCode(String phoneNumber);

    boolean isCodeValid(String phoneNumber, String code);

    void consumeCode(String phoneNumber);
}
