package com.university.fintech.infrastructure.primary.restful.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    private String verificationCode;

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
