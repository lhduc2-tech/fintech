package com.university.fintech.infrastructure.primary.restful.response.admin.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {

    private UUID id;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String identityNumber;
    private LocalDate dob;
    private String hometown;
    private String status;
    private Integer tier;
    private BigDecimal riskScore;
    private Integer failedLoginAttempts;
    private Integer pinFailedAttempts;
    private boolean hasPin;
    private String ekycStatus;
    private OffsetDateTime ekycRegisteredAt;
    private Set<String> roleCodes;
    private Set<String> permissionCodes;

    private WalletInfo wallet;

    private OffsetDateTime createdAt;
    private String createdBy;
    private OffsetDateTime updatedAt;
    private String updatedBy;
    private OffsetDateTime lastLoginAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletInfo {
        private UUID walletId;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private BigDecimal holdingBalance;
        private String walletStatus;
        private Integer limitation;
    }
}
