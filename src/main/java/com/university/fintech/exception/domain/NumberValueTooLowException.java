package com.university.fintech.exception.domain;

import java.util.Map;

public final class NumberValueTooLowException extends AssertionException {
    private final String min;
    private final String value;

    private NumberValueTooLowException(NumberValueTooLowExceptionBuilder builder) {
        super(builder.field, builder.message());
        this.min = builder.minValue;
        this.value = builder.value;
    }

    public static NumberValueTooLowExceptionBuilder builder() {
        return new NumberValueTooLowExceptionBuilder();
    }

    public AssertionErrorType type() {
        return AssertionErrorType.NUMBER_VALUE_TOO_LOW;
    }

    public Map<String, String> parameters() {
        return Map.of("min", this.min, "value", this.value);
    }

    public static class NumberValueTooLowExceptionBuilder {
        private String field;
        private String minValue;
        private String value;

        public NumberValueTooLowExceptionBuilder field(String field) {
            this.field = field;
            return this;
        }

        public NumberValueTooLowExceptionBuilder minValue(String minValue) {
            this.minValue = minValue;
            return this;
        }

        public NumberValueTooLowExceptionBuilder value(String value) {
            this.value = value;
            return this;
        }

        public String message() {
            return "Value of field \"" + this.field + "\" must be at least " + this.minValue + " but was " + this.value;
        }

        public NumberValueTooLowException build() {
            return new NumberValueTooLowException(this);
        }
    }
}