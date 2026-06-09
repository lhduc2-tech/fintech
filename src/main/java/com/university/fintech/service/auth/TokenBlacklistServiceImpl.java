package com.university.fintech.service.auth;

import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.domain.port.primary.TokenBlacklistService;
import com.university.fintech.wire.security.impl.JwtUtil;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisRestTokenStore tokenStore;
    private final JwtUtil jwtUtil;

    @Override
    public void blacklist(String token) {
        long ttlSeconds = Math.max(1, jwtUtil.getRemainingValidityMillis(token) / 1000);
        tokenStore.set(Constant.TOKEN_BLACKLIST_REDIS_PREFIX + token, Constant.TOKEN_BLACKLIST_VALUE, ttlSeconds);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return tokenStore.get(Constant.TOKEN_BLACKLIST_REDIS_PREFIX + token) != null;
    }
}
