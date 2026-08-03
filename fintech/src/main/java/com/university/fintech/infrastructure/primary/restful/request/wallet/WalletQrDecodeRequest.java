package com.university.fintech.infrastructure.primary.restful.request.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body cho endpoint decode QR code.
 * Client tự quét QR bằng camera device, lấy raw string rồi gửi lên đây.
 * Backend sẽ parse JSON trong {@code qrContent} và trả về thông tin người nhận.
 */
@Getter
@Setter
public class WalletQrDecodeRequest {

    /**
     * Nội dung text đọc được từ QR code.
     * Expected format: JSON string {@code {"phoneNumber":"...","fullName":"..."}}.
     */
    @NotBlank
    @Size(max = 500)
    private String qrContent;
}
