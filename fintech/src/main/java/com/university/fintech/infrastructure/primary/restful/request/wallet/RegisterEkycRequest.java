package com.university.fintech.infrastructure.primary.restful.request.wallet;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request đăng ký eKYC sinh trắc học 5 góc mặt.
 *
 * <p>Client chụp lần lượt 5 góc: quay trái, quay phải, cúi, ngửa, nhìn thẳng.
 * Mỗi ảnh encode thành base64 (data:image/jpeg;base64,...).
 */
@Getter
@Setter
public class RegisterEkycRequest {

    /**
     * Danh sách ảnh base64 — phải đúng 5 ảnh (5 góc mặt theo chuẩn eKYC).
     * Định dạng: {@code data:image/jpeg;base64,...} hoặc chuỗi base64 thuần.
     */
    @NotEmpty(message = "Danh sach anh khong duoc de trong.")
    @Size(min = 5, max = 5, message = "Phai cung cap dung 5 anh cho 5 goc mat eKYC.")
    private List<String> images;
}
