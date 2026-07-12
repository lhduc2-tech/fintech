package com.university.fintech.domain.port.primary;

public interface TokenBlacklistService {

    void blacklist(String token);

    boolean isBlacklisted(String token);
}
