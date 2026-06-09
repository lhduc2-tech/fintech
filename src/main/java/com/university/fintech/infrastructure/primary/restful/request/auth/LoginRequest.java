package com.university.fintech.infrastructure.primary.restful.request.auth;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    @Pattern(regexp = Constant.PHONE_NUMBER_NUMERIC_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_NUMERIC_ONLY)
    @Pattern(regexp = Constant.PHONE_NUMBER_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS)
    private String phoneNumber;

    @NotBlank
    private String password;

    private String verificationCode;
}
