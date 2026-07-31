package com.university.fintech.infrastructure.primary.restful.request.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmCreatePinRequest {

    @NotBlank
    @Size(min = 6, max = 6)
    private String otpCode;
}
