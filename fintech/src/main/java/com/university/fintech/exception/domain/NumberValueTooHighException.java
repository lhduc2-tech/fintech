package com.university.fintech.exception.domain;

import java.util.Map;

public final class NumberValueTooHighException extends AssertionException {
    private final String max;
    private final String value;

    private NumberValueTooHighException(NumberValueTooHighExceptionBuilder builder) {
        super(builder.field, builder.message());
        this.max = builder.maxValue;
        this.value = builder.value;
    }

    public static NumberValueTooHighExceptionBuilder builder() {
        return new NumberValueTooHighExceptionBuilder();
    }

    public AssertionErrorType type() {
        return AssertionErrorType.NUMBER_VALUE_TOO_HIGH;
    }

    public Map<String, String> parameters() {
        return Map.of("max", this.max, "value", this.value);
    }

    public static class NumberValueTooHighExceptionBuilder {
        private String field;
        private String maxValue;
        private String value;

        public NumberValueTooHighExceptionBuilder field(String field) {
            this.field = field;
            return this;
        }

        public NumberValueTooHighExceptionBuilder maxValue(String maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public NumberValueTooHighExceptionBuilder value(String value) {
            this.value = value;
            return this;
        }

        public String message() {
            return "Value of field \"" + this.field + "\" must be at most " + this.maxValue + " but was " + this.value;
        }

        public NumberValueTooHighException build() {
            return new NumberValueTooHighException(this);
        }
    }
}
