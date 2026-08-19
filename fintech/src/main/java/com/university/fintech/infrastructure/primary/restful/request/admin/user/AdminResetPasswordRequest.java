package com.university.fintech.infrastructure.primary.restful.request.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResetPasswordRequest {

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
