package com.university.fintech.exception.domain;

public class NullElementInCollectionException extends AssertionException {
    public NullElementInCollectionException(String field) {
        super(field, message(field));
    }

    private static String message(String field) {
        return "The field \"" + field + "\" contains a null element";
    }

    public AssertionErrorType type() {
        return AssertionErrorType.NULL_ELEMENT_IN_COLLECTION;
    }
}
