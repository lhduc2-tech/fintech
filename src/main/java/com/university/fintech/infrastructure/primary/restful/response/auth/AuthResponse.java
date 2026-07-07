package com.university.fintech.infrastructure.primary.restful.response.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class AuthResponse {

    private UUID userId;

    private String email;

    private String fullName;

    private String status;

    private Integer remainingLoginAttempts;

    private Set<String> roles;

    private String accessToken;

    private String tokenType;

    private String identityNumber;

    private LocalDate dob;

    private String hometown;

    private Integer tier;
}
