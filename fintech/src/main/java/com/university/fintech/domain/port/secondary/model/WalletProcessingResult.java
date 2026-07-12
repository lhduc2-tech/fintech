package com.university.fintech.domain.port.secondary.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletProcessingResult {

    private String status;

    private String message;
}
