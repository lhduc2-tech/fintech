package com.university.fintech.infrastructure.primary.restful.response.wallet;

import lombok.Builder;
import lombok.Getter;

/**
 * Response sau khi xác thực FaceID thành công.
 *
 * <p>Client phải lưu {@code faceIdToken} và gửi kèm trong request giao dịch
 * (field {@code faceIdToken} của {@code WalletTransferRequest} hoặc {@code WalletBankTransferRequest}).
 *
 * <p>Token chỉ dùng được <b>một lần</b> và hết hạn sau {@code expiresInSeconds} giây.
 */
@Getter
@Builder
public class VerifyFaceIdResponse {

    /**
     * Short-lived one-time token để kèm vào request giao dịch.
     * Hết hạn sau {@code expiresInSeconds} giây.
     */
    private String faceIdToken;

    /** Thời gian hết hạn của token tính bằng giây (mặc định 300s = 5 phút). */
    private long expiresInSeconds;

    /** Thông báo thành công để hiển thị cho user. */
    private String message;
}
