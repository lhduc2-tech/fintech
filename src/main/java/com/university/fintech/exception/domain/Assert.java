package com.university.fintech.exception.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Assert {
    private Assert() {
    }

    public static void notNull(String field, Object input) {
        if (input == null) {
            throw MissingMandatoryValueException.forNullValue(field);
        }
    }

    public static void notBlank(String field, String input) {
        field(field, input).notBlank();
    }

    public static void notEmpty(String field, Collection<?> collection) {
        field(field, collection).notEmpty();
    }

    public static void notEmpty(String field, Map<?, ?> map) {
        notNull(field, map);
        if (map.isEmpty()) {
            throw MissingMandatoryValueException.forEmptyValue(field);
        }
    }

    public static StringAsserter field(String field, String input) {
        return new StringAsserter(field, input);
    }

    public static IntegerAsserter field(String field, Integer input) {
        return new IntegerAsserter(field, input);
    }

    public static LongAsserter field(String field, Long input) {
        return new LongAsserter(field, input);
    }

    public static FloatAsserter field(String field, Float input) {
        return new FloatAsserter(field, input);
    }

    public static DoubleAsserter field(String field, Double input) {
        return new DoubleAsserter(field, input);
    }

    public static BigDecimalAsserter field(String field, BigDecimal input) {
        return new BigDecimalAsserter(field, input);
    }

    public static <T> CollectionAsserter<T> field(String field, Collection<T> input) {
        return new CollectionAsserter<T>(field, input);
    }

    public static <T> ArrayAsserter<T> field(String field, T[] input) {
        return new ArrayAsserter<T>(field, input);
    }

    public static InstantAsserter field(String field, Instant input) {
        return new InstantAsserter(field, input);
    }

    public static final class StringAsserter {
        private final String field;
        private final String value;

        private StringAsserter(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public StringAsserter notNull() {
            Assert.notNull(this.field, this.value);
            return this;
        }

        public StringAsserter notBlank() {
            this.notNull();
            if (this.value.isBlank()) {
                throw MissingMandatoryValueException.forBlankValue(this.field);
            } else {
                return this;
            }
        }

        public StringAsserter minLength(int length) {
            if (length <= 0 && this.value == null) {
                return this;
            } else {
                this.notNull();
                if (this.value.length() < length) {
                    throw StringTooShortException.builder().field(this.field).value(this.value).minLength(length).build();
                } else {
                    return this;
                }
            }
        }

        public StringAsserter maxLength(int length) {
            if (this.value == null) {
                return this;
            } else if (this.value.length() > length) {
                throw StringTooLongException.builder().field(this.field).value(this.value).maxLength(length).build();
            } else {
                return this;
            }
        }
    }

    public static final class IntegerAsserter {
        private final String field;
        private final Integer value;

        private IntegerAsserter(String field, Integer value) {
            this.field = field;
            this.value = value;
        }

        public IntegerAsserter positive() {
            return this.min(0);
        }

        public IntegerAsserter min(int minValue) {
            Assert.notNull(this.field, this.value);
            if (this.value < minValue) {
                throw NumberValueTooLowException.builder().field(this.field).minValue(String.valueOf(minValue)).value(String.valueOf(this.value)).build();
            } else {
                return this;
            }
        }

        public IntegerAsserter max(int maxValue) {
            Assert.notNull(this.field, this.value);
            if (this.value > maxValue) {
                throw NumberValueTooHighException.builder().field(this.field).maxValue(String.valueOf(maxValue)).value(String.valueOf(this.value)).build();
            } else {
                return this;
            }
        }
    }

    public static final class LongAsserter {
        private final String field;
        private final Long value;

        private LongAsserter(String field, Long value) {
            this.field = field;
            this.value = value;
        }

        public LongAsserter positive() {
            return this.min(0L);
        }

        public LongAsserter min(long minValue) {
            Assert.notNull(this.field, this.value);
            if (this.value < minValue) {
                throw NumberValueTooLowException.builder().field(this.field).minValue(String.valueOf(minValue)).value(String.valueOf(this.value)).build();
            } else {
                return this;
            }
        }

        public LongAsserter max(long maxValue) {
            Assert.notNull(this.field, this.value);
            if (this.value > maxValue) {
                throw NumberValueTooHighException.builder().field(this.field).maxValue(String.valueOf(maxValue)).value(String.valueOf(this.value)).build();
            } else {
                return this;
            }
        }
    }

    public static final class FloatAsserter {
        private final String field;
        private final Float value;

        private FloatAsserter(String field, Float value) {
            this.field = field;
            this.value = value;
        }

        public FloatAsserter positive() {
            return this.min(0.0F);
        }

        public FloatAsserter strictlyPositive() {
            return this.over(0.0F);
        }

        public FloatAsserter min(float minValue) {
            Assert.notNull(this.field, this.value);
            if (this.value < minValue) {
                throw this.tooLow(minValue);
            } else {
                return this;
            }
        }

        public FloatAsserter over(float floor) {
            Assert.notNull(this.field, this.value);
            if (this.value <= floor) {
                throw this.tooLow(floor);
            } else {
                return this;
            }
        }

        private NumberValueTooLowException tooLow(float floor) {
            return NumberValueTooLowException.builder().field(this.field).minValue(String.valueOf(floor)).value(String.valueOf(this.value)).build();
        }

        public FloatAsserter max(float maxValue) {
            Assert.notNull(this.field, this.value);
            if (this.value > maxValue) {
                throw this.tooHigh(maxValue);
            } else {
                return this;
            }
        }

        public FloatAsserter under(float ceil) {
            Assert.notNull(this.field, this.value);
            if (this.value >= ceil) {
                throw this.tooHigh(ceil);
            } else {
                return this;
            }
        }

        private NumberValueTooHighException tooHigh(float ceil) {
            return NumberValueTooHighException.builder().field(this.field).maxValue(String.valueOf(ceil)).value(String.valueOf(this.value)).build();
        }
    }

    public static final class DoubleAsserter {
        private final String field;
        private final Double value;

        private DoubleAsserter(String field, Double value) {
            this.field = field;
            this.value = value;
        }

        public DoubleAsserter positive() {
            return this.min((double)0.0F);
        }

        public DoubleAsserter strictlyPositive() {
            return this.over((double)0.0F);
        }

        public DoubleAsserter min(double minValue) {
            Assert.notNull(this.field, this.value);
            if (this.value < minValue) {
                throw this.tooLow(minValue);
            } else {
                return this;
            }
        }

        public DoubleAsserter over(double floor) {
            Assert.notNull(this.field, this.value);
            if (this.value <= floor) {
                throw this.tooLow(floor);
            } else {
                return this;
            }
        }

        private NumberValueTooLowException tooLow(double floor) {
            return NumberValueTooLowException.builder().field(this.field).minValue(String.valueOf(floor)).value(String.valueOf(this.value)).build();
        }

        public DoubleAsserter max(double maxValue) {
            Assert.notNull(this.field, this.value);
            if (this.value > maxValue) {
                throw this.tooHigh(maxValue);
            } else {
                return this;
            }
        }

        public DoubleAsserter under(double ceil) {
            Assert.notNull(this.field, this.value);
            if (this.value >= ceil) {
                throw this.tooHigh(ceil);
            } else {
                return this;
            }
        }

        private NumberValueTooHighException tooHigh(double ceil) {
            return NumberValueTooHighException.builder().field(this.field).maxValue(String.valueOf(ceil)).value(String.valueOf(this.value)).build();
        }
    }

    public static final class BigDecimalAsserter {
        private final String field;
        private final BigDecimal value;

        private BigDecimalAsserter(String field, BigDecimal value) {
            this.field = field;
            this.value = value;
        }

        public BigDecimalAsserter positive() {
            return this.min(0L);
        }

        public BigDecimalAsserter strictlyPositive() {
            return this.over(0L);
        }

        public BigDecimalAsserter min(long minValue) {
            return this.min(new BigDecimal(minValue));
        }

        public BigDecimalAsserter min(BigDecimal minValue) {
            this.notNull();
            Assert.notNull("minValue", minValue);
            if (this.value.compareTo(minValue) < 0) {
                throw this.tooLow(minValue);
            } else {
                return this;
            }
        }

        public BigDecimalAsserter over(long floor) {
            return this.over(new BigDecimal(floor));
        }

        public BigDecimalAsserter over(BigDecimal floor) {
            this.notNull();
            Assert.notNull("floor", floor);
            if (this.value.compareTo(floor) <= 0) {
                throw this.tooLow(floor);
            } else {
                return this;
            }
        }

        private NumberValueTooLowException tooLow(BigDecimal floor) {
            return NumberValueTooLowException.builder().field(this.field).minValue(String.valueOf(floor)).value(this.value.toPlainString()).build();
        }

        public BigDecimalAsserter max(long maxValue) {
            return this.max(new BigDecimal(maxValue));
        }

        public BigDecimalAsserter max(BigDecimal maxValue) {
            this.notNull();
            Assert.notNull("maxValue", maxValue);
            if (this.value.compareTo(maxValue) > 0) {
                throw this.tooHigh(maxValue);
            } else {
                return this;
            }
        }

        public BigDecimalAsserter under(long ceil) {
            return this.under(new BigDecimal(ceil));
        }

        public BigDecimalAsserter under(BigDecimal ceil) {
            this.notNull();
            Assert.notNull("ceil", ceil);
            if (this.value.compareTo(ceil) >= 0) {
                throw this.tooHigh(ceil);
            } else {
                return this;
            }
        }

        private NumberValueTooHighException tooHigh(BigDecimal ceil) {
            return NumberValueTooHighException.builder().field(this.field).maxValue(String.valueOf(ceil)).value(this.value.toPlainString()).build();
        }

        public BigDecimalAsserter notNull() {
            Assert.notNull(this.field, this.value);
            return this;
        }
    }

    public static final class CollectionAsserter<T> {
        private final String field;
        private final Collection<T> value;

        private CollectionAsserter(String field, Collection<T> value) {
            this.field = field;
            this.value = value;
        }

        public CollectionAsserter<T> notNull() {
            Assert.notNull(this.field, this.value);
            return this;
        }

        public CollectionAsserter<T> notEmpty() {
            this.notNull();
            if (this.value.isEmpty()) {
                throw MissingMandatoryValueException.forEmptyValue(this.field);
            } else {
                return this;
            }
        }

        public CollectionAsserter<T> maxSize(int maxSize) {
            if (maxSize <= 0 && this.value == null) {
                return this;
            } else {
                this.notNull();
                if (this.value.size() > maxSize) {
                    throw TooManyElementsException.builder().field(this.field).maxSize(maxSize).size(this.value.size()).build();
                } else {
                    return this;
                }
            }
        }

        public CollectionAsserter<T> noNullElement() {
            if (this.value == null) {
                return this;
            } else if (this.value.stream().anyMatch(Objects::isNull)) {
                throw new NullElementInCollectionException(this.field);
            } else {
                return this;
            }
        }
    }

    public static final class ArrayAsserter<T> {
        private final String field;
        private final T[] value;

        private ArrayAsserter(String field, T[] value) {
            this.field = field;
            this.value = value;
        }

        public ArrayAsserter<T> notNull() {
            Assert.notNull(this.field, this.value);
            return this;
        }

        public ArrayAsserter<T> notEmpty() {
            this.notNull();
            if (this.value.length == 0) {
                throw MissingMandatoryValueException.forEmptyValue(this.field);
            } else {
                return this;
            }
        }

        public ArrayAsserter<T> maxSize(int maxSize) {
            if (maxSize <= 0 && this.value == null) {
                return this;
            } else {
                this.notNull();
                if (this.value.length > maxSize) {
                    throw TooManyElementsException.builder().field(this.field).maxSize(maxSize).size(this.value.length).build();
                } else {
                    return this;
                }
            }
        }

        public ArrayAsserter<T> noNullElement() {
            if (this.value == null) {
                return this;
            } else if (Stream.of(this.value).anyMatch(Objects::isNull)) {
                throw new NullElementInCollectionException(this.field);
            } else {
                return this;
            }
        }
    }

    public static final class InstantAsserter {
        private static final String OTHER_FIELD_NAME = "other";
        private final String field;
        private final Instant value;

        private InstantAsserter(String field, Instant value) {
            this.field = field;
            this.value = value;
        }

        public InstantAsserter inFuture() {
            return this.afterOrAt(Instant.now());
        }

        public InstantAsserter after(Instant other) {
            this.notNull();
            Assert.notNull("other", other);
            if (this.value.compareTo(other) <= 0) {
                throw NotAfterTimeException.strictlyNotAfter().value(this.value).field(this.field).other(other);
            } else {
                return this;
            }
        }

        public InstantAsserter afterOrAt(Instant other) {
            this.notNull();
            Assert.notNull("other", other);
            if (this.value.compareTo(other) < 0) {
                throw NotAfterTimeException.notAfter().value(this.value).field(this.field).other(other);
            } else {
                return this;
            }
        }

        public InstantAsserter inPast() {
            return this.beforeOrAt(Instant.now());
        }

        public InstantAsserter before(Instant other) {
            this.notNull();
            Assert.notNull("other", other);
            if (this.value.compareTo(other) >= 0) {
                throw NotBeforeTimeException.strictlyNotBefore().value(this.value).field(this.field).other(other);
            } else {
                return this;
            }
        }

        public InstantAsserter beforeOrAt(Instant other) {
            this.notNull();
            Assert.notNull("other", other);
            if (this.value.compareTo(other) > 0) {
                throw NotBeforeTimeException.notBefore().value(this.value).field(this.field).other(other);
            } else {
                return this;
            }
        }

        public InstantAsserter notNull() {
            Assert.notNull(this.field, this.value);
            return this;
        }
    }
}
