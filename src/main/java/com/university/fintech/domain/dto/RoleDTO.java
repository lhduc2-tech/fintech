package com.university.fintech.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@SuppressWarnings("unused")
public class RoleDTO {

    private String code;

    private String name;

    private String description;

    private String status;

    private Set<String> permissionCodes;
}

