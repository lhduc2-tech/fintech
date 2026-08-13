package com.university.fintech.domain.port.primary;

import java.util.List;
import java.util.UUID;

/**
 * Port giao tiếp với hệ thống FaceID / eKYC.
 *
 * <p>Implementation gọi HTTP đến FaceID Python server (faceid_2) và quản lý
 * short-lived token trong Redis để xác thực giao dịch cao (≥ 10M VNĐ).
 */
public interface FaceIdService {

    /**
     * Đăng ký eKYC sinh trắc học 5 góc mặt cho user.
     * Gọi {@code POST /api/register_ekyc} trên FaceID Python server.
     *
     * @param userId       ID của user cần đăng ký
     * @param imagesBase64 Danh sách 5 ảnh base64 (data:image/jpeg;base64,...)
     * @throws com.university.fintech.exception.domain.AuthException nếu FaceID server lỗi hoặc vi phạm quy chuẩn eKYC
     */
    void registerEkyc(UUID userId, List<String> imagesBase64);

    /**
     * Xác thực khuôn mặt 2 trạm (Anti-Spoofing + Cosine Match 512D).
     * Nếu thành công, tạo short-lived FaceID token (TTL 5 phút) lưu vào Redis.
     *
     * @param userId               ID của user cần xác thực
     * @param imagesBase64         Danh sách ảnh camera (≥ 2 frames để chống deepfake)
     * @param activeLivenessPassed {@code true} nếu client đã hoàn tất liveness check chủ động (xoay mặt)
     * @return Chuỗi FaceID token (UUID), client cần gửi kèm request giao dịch
     * @throws com.university.fintech.exception.domain.AuthException nếu xác thực thất bại hoặc server lỗi
     */
    String verifyAndIssueToken(UUID userId, List<String> imagesBase64, boolean activeLivenessPassed);

    /**
     * Kiểm tra FaceID token còn hiệu lực không (Redis lookup).
     *
     * @param userId       ID của user
     * @param faceIdToken  Token nhận từ {@link #verifyAndIssueToken}
     * @return {@code true} nếu token hợp lệ và chưa hết hạn
     */
    boolean isTokenValid(UUID userId, String faceIdToken);

    /**
     * Tiêu thụ (xóa) FaceID token sau khi giao dịch được chấp thuận.
     * Token là one-time use — một token chỉ xác thực được một giao dịch.
     *
     * @param userId      ID của user
     * @param faceIdToken Token cần xóa
     */
    void consumeToken(UUID userId, String faceIdToken);
}
