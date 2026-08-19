package com.university.fintech.infrastructure.primary.restful.request.admin.user;

import com.university.fintech.infrastructure.primary.restful.request.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdminSearchUserRequest extends BaseRequest {

    private String search;

    private String status;

    private Integer tier;

    private String ekycStatus;

    private String roleCode;

    private OffsetDateTime fromCreatedAt;

    private OffsetDateTime toCreatedAt;
}
