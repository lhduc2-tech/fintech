package com.university.fintech.infrastructure.primary.restful.request.admin.user;

import com.university.fintech.shared.constant.Constant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class AdminCreateUserRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Size(max = 255)
    private String fullName;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = Constant.PHONE_NUMBER_NUMERIC_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_NUMERIC_ONLY)
    @Pattern(regexp = Constant.PHONE_NUMBER_PATTERN, message = Constant.MESSAGE_PHONE_NUMBER_EXACTLY_10_DIGITS)
    private String phoneNumber;

    @Size(max = 50)
    private String identityNumber;

    private LocalDate dob;

    @Size(max = 255)
    private String hometown;

    private Integer tier;

    private BigDecimal riskScore;

    private String status;

    private Set<String> roleCodes;
}
