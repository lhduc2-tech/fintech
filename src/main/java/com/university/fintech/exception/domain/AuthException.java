package com.university.fintech.exception.domain;

import java.util.Collections;
import java.util.Map;

public class AuthException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> details;

    public AuthException(String message, String errorCode) {
        this(message, errorCode, Collections.emptyMap());
    }

    public AuthException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? Collections.emptyMap() : Map.copyOf(details);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
