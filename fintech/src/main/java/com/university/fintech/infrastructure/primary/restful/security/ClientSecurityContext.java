package com.university.fintech.infrastructure.primary.restful.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSecurityContext {
    private String deviceId;
    private String deviceFingerprint;
    private String clientIp;
    private String geoLocation;
}
