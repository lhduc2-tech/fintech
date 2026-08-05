package com.university.fintech.infrastructure.primary.restful.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response sau khi decode QR code của người nhận.
 * Client dùng thông tin này để điền tự động vào form chuyển tiền.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletQrDecodeResponse {

    /** Số điện thoại của người nhận (dùng để thực hiện chuyển tiền). */
    private String phoneNumber;

    /** Tên đầy đủ của người nhận (để hiển thị xác nhận trước khi chuyển). */
    private String fullName;
}
