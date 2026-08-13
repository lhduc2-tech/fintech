package com.university.fintech.infrastructure.primary.restful.request.wallet;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request xác thực FaceID để lấy short-lived token cho giao dịch cao (≥ 10M VNĐ).
 *
 * <p>Client gửi ≥ 2 frame từ camera để hệ thống thực hiện:
 * <ul>
 *   <li>Trạm 1: Anti-Spoofing (MiniFASNet) + Active 3D Motion Check</li>
 *   <li>Trạm 2: Cosine Similarity 512D so với Cloud DB</li>
 * </ul>
 */
@Getter
@Setter
public class VerifyFaceIdRequest {

    /**
     * Danh sách frames từ camera (base64).
     * Cần ít nhất 2 frames để chống ảnh in / deepfake (passive liveness).
     */
    @NotEmpty(message = "Danh sach anh khong duoc de trong.")
    @Size(min = 2, max = 10, message = "Phai cung cap tu 2 den 10 frames anh.")
    private List<String> images;

    /**
     * {@code true} nếu client đã yêu cầu user thực hiện liveness chủ động
     * (xoay mặt ≥ 7° hoặc chớp mắt) — tăng độ tin cậy chống deepfake.
     */
    private boolean activeLivenessPassed = false;
}
