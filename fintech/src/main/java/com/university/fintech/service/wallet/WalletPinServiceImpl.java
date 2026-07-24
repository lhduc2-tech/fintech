package com.university.fintech.service.wallet;

import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.port.primary.AuthMailService;
import com.university.fintech.domain.port.primary.WalletPinOtpService;
import com.university.fintech.domain.port.primary.WalletPinService;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.redis.RedisRestTokenStore;
import com.university.fintech.shared.constant.Constant;
import com.university.fintech.wire.security.Password;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletPinServiceImpl implements WalletPinService {

    private final UserRepository userRepository;
    private final Password password;
    private final WalletPinOtpService walletPinOtpService;
    private final AuthMailService authMailService;
    private final RedisRestTokenStore tokenStore;

    @Value(Constant.WALLET_PIN_MAX_FAILED_ATTEMPTS_PROPERTY)
    private int maxPinFailedAttempts;

    @Value(Constant.WALLET_PIN_PENDING_TTL_SECONDS_PROPERTY)
    private long pinPendingTtlSeconds;

    @Override
    @Transactional(readOnly = true)
    public void requestCreatePin(UUID userId, String pin) {
        validatePinFormat(pin);

        UserEntity user = findUser(userId);

        // Hash PIN tạm lưu vào Redis chờ xác nhận OTP
        String hashedPin = password.encode(pin);
        tokenStore.set(buildPinPendingKey(userId), hashedPin, pinPendingTtlSeconds);

        // Tạo OTP và gửi email
        String otp = walletPinOtpService.createOtp(userId.toString());
        authMailService.sendPinOtpCode(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    @Transactional
    public void confirmCreatePin(UUID userId, String otpCode) {
        // Xác thực OTP
        if (!walletPinOtpService.isOtpValid(userId.toString(), otpCode.trim())) {
            throw new AuthException(Constant.MESSAGE_INVALID_PIN_OTP, Constant.ERROR_INVALID_PIN_OTP);
        }

        // Lấy PIN hash tạm từ Redis
        String pendingPinHash = tokenStore.get(buildPinPendingKey(userId));
        if (pendingPinHash == null || pendingPinHash.isBlank()) {
            throw new AuthException(Constant.MESSAGE_PIN_OTP_REQUIRED, Constant.ERROR_PIN_OTP_REQUIRED);
        }

        // Lưu PIN hash vào DB và reset pin_failed_attempts
        UserEntity user = findUser(userId);
        user.setPinHash(pendingPinHash);
        user.setPinFailedAttempts(0);
        userRepository.save(user);

        // Xóa Redis keys
        walletPinOtpService.consumeOtp(userId.toString());
        tokenStore.delete(buildPinPendingKey(userId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = AuthException.class)
    public boolean verifyPin(UUID userId, String pin) {
        UserEntity user = findUser(userId);

        // Kiểm tra PIN có bị khóa không
        int failedAttempts = user.getPinFailedAttempts() == null ? 0 : user.getPinFailedAttempts();
        if (failedAttempts >= maxPinFailedAttempts) {
            throw new AuthException(Constant.MESSAGE_PIN_LOCKED, Constant.ERROR_PIN_LOCKED);
        }

        // Xác thực PIN
        if (!password.matches(pin, user.getPinHash())) {
            user.setPinFailedAttempts(failedAttempts + 1);
            userRepository.save(user);

            // Kiểm tra lại sau khi tăng
            if (user.getPinFailedAttempts() >= maxPinFailedAttempts) {
                throw new AuthException(Constant.MESSAGE_PIN_LOCKED, Constant.ERROR_PIN_LOCKED);
            }
            return false;
        }

        // PIN đúng: reset counter
        if (failedAttempts > 0) {
            user.setPinFailedAttempts(0);
            userRepository.save(user);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPin(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getPinHash() != null && !user.getPinHash().isBlank())
                .orElse(false);
    }

    private UserEntity findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));
    }

    private void validatePinFormat(String pin) {
        if (pin == null || !pin.matches(Constant.WALLET_PIN_PATTERN)) {
            throw new AuthException(Constant.MESSAGE_INVALID_PIN_FORMAT, Constant.ERROR_INVALID_PIN_FORMAT);
        }
    }

    private String buildPinPendingKey(UUID userId) {
        return Constant.WALLET_PIN_PENDING_REDIS_PREFIX + userId;
    }
}
