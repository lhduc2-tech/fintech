package com.university.fintech.exception.domain;

import java.time.Instant;

public final class NotBeforeTimeException extends AssertionException {
    private NotBeforeTimeException(String field, String message) {
        super(field, message);
    }

    public static NotBeforeTimeExceptionValueBuilder strictlyNotBefore() {
        return new NotBeforeTimeExceptionBuilder("must be strictly before");
    }

    public static NotBeforeTimeExceptionValueBuilder notBefore() {
        return new NotBeforeTimeExceptionBuilder("must be before");
    }

    public AssertionErrorType type() {
        return AssertionErrorType.NOT_BEFORE_TIME;
    }

    public static final class NotBeforeTimeExceptionBuilder implements NotBeforeTimeExceptionValueBuilder, NotBeforeTimeExceptionFieldBuilder, NotBeforeTimeExceptionOtherBuilder {
        private final String hint;
        private Instant value;
        private String field;
        private Instant other;

        private NotBeforeTimeExceptionBuilder(String hint) {
            this.hint = hint;
        }

        public NotBeforeTimeExceptionFieldBuilder value(Instant value) {
            this.value = value;
            return this;
        }

        public NotBeforeTimeExceptionOtherBuilder field(String field) {
            this.field = field;
            return this;
        }

        public NotBeforeTimeException other(Instant other) {
            this.other = other;
            return this.build();
        }

        private NotBeforeTimeException build() {
            return new NotBeforeTimeException(this.field, this.message());
        }

        private String message() {
            return "Time %s in \"%s\" %s %s but wasn't".formatted(this.value, this.field, this.hint, this.other);
        }
    }

    public interface NotBeforeTimeExceptionFieldBuilder {
        NotBeforeTimeExceptionOtherBuilder field(String field);
    }

    public interface NotBeforeTimeExceptionOtherBuilder {
        NotBeforeTimeException other(Instant other);
    }

    public interface NotBeforeTimeExceptionValueBuilder {
        NotBeforeTimeExceptionFieldBuilder value(Instant value);
    }
}
