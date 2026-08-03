package com.university.fintech.infrastructure.primary.restful.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response khi generate QR code cho ví.
 * {@code qrBase64} là ảnh PNG được encode thành Base64 string,
 * client có thể dùng trực tiếp trong {@code <img src="data:image/png;base64,...">}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletQrResponse {

    /** Ảnh QR code PNG encode Base64. */
    private String qrBase64;

    /** Số điện thoại được encode vào QR (để client hiển thị kèm). */
    private String phoneNumber;

    /** Tên hiển thị của chủ ví (để client hiển thị kèm). */
    private String fullName;
}
