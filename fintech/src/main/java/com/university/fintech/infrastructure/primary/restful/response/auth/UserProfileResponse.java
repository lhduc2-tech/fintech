package com.university.fintech.infrastructure.primary.restful.response.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserProfileResponse {

    private String fullName;

    private String identityNumber;

    private LocalDate dob;

    private String hometown;

    private String email;

    private String phoneNumber;

    private Integer tier;
}
