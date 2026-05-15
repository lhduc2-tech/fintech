package com.university.fintech.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionDTO {

    private String code;

    private String description;
}
