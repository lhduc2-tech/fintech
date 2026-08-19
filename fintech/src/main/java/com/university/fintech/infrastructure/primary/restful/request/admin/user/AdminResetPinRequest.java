package com.university.fintech.infrastructure.primary.restful.request.admin.user;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResetPinRequest {

    /**
     * Mã PIN 6 chữ số mới. Nếu để trống hoặc null, mã PIN hiện tại của người dùng sẽ bị xóa để người dùng tạo mới.
     */
    @Pattern(regexp = Constant.WALLET_PIN_PATTERN, message = Constant.MESSAGE_INVALID_PIN_FORMAT)
    private String newPin;
}
