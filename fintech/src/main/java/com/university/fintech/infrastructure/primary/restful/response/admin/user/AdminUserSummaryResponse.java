package com.university.fintech.infrastructure.primary.restful.response.admin.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryResponse {

    private UUID id;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String identityNumber;
    private String status;
    private Integer tier;
    private BigDecimal riskScore;
    private String ekycStatus;
    private Integer failedLoginAttempts;
    private Integer pinFailedAttempts;
    private Set<String> roleCodes;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLoginAt;
}
