package com.university.fintech.infrastructure.primary.restful.request.admin.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class AdminUpdateUserRequest {

    @Size(max = 255)
    private String fullName;

    @Size(max = 50)
    private String identityNumber;

    private LocalDate dob;

    @Size(max = 255)
    private String hometown;

    private Integer tier;

    private BigDecimal riskScore;

    private String ekycStatus;

    private Set<String> roleCodes;
}
