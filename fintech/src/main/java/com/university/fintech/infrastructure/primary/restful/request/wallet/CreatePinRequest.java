package com.university.fintech.infrastructure.primary.restful.request.wallet;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePinRequest {

    @NotBlank(message = Constant.MESSAGE_INVALID_PIN_FORMAT)
    @Pattern(regexp = Constant.WALLET_PIN_PATTERN, message = Constant.MESSAGE_INVALID_PIN_FORMAT)
    @Size(min = 6, max = 6, message = Constant.MESSAGE_INVALID_PIN_FORMAT)
    private String pin;
}
