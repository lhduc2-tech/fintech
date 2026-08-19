package com.university.fintech.infrastructure.primary.restful.request.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserStatusRequest {

    @NotBlank
    @Size(max = 50)
    private String status;

    @Size(max = 500)
    private String reason;
}
