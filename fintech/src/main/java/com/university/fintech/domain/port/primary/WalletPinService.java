package com.university.fintech.domain.port.primary;

import java.util.UUID;

public interface WalletPinService {

    /**
     * Bước 1 tạo PIN: validate format PIN, hash tạm vào Redis, gửi OTP qua email.
     */
    void requestCreatePin(UUID userId, String pin);

    /**
     * Bước 2 tạo PIN: xác thực OTP, lưu PIN hash vào DB.
     */
    void confirmCreatePin(UUID userId, String otpCode);

    /**
     * Xác thực PIN khi thực hiện giao dịch.
     * Trả về true nếu PIN đúng, false nếu sai (và tăng counter sai).
     * Ném exception nếu PIN bị khóa.
     */
    boolean verifyPin(UUID userId, String pin);

    /**
     * Kiểm tra user đã tạo PIN hay chưa.
     */
    boolean hasPin(UUID userId);
}
