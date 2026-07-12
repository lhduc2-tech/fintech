package com.university.fintech.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private UUID id;

    private String code;

    private String description;

    @Builder.Default
    private Set<String> roleCodes = new HashSet<>();

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}
