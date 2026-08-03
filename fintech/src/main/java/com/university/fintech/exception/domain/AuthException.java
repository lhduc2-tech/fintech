package com.university.fintech.exception.domain;

import java.util.Collections;
import java.util.Map;

public class AuthException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> details;
    private final boolean retryable;

    public AuthException(String message, String errorCode) {
        this(message, errorCode, false, Collections.emptyMap());
    }

    public AuthException(String message, String errorCode, Map<String, Object> details) {
        this(message, errorCode, false, details);
    }

    public AuthException(String message, String errorCode, boolean retryable) {
        this(message, errorCode, retryable, Collections.emptyMap());
    }

    public AuthException(String message, String errorCode, boolean retryable, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
        this.details = details == null ? Collections.emptyMap() : Map.copyOf(details);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
