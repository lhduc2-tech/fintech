package com.university.fintech.exception.domain;

import java.util.Map;

public abstract class AssertionException extends RuntimeException {
    private final String field;

    protected AssertionException(String field, String message) {
        super(message);
        this.field = field;
    }

    public abstract AssertionErrorType type();

    public String field() {
        return this.field;
    }

    public Map<String, String> parameters() {
        return Map.of();
    }
}
