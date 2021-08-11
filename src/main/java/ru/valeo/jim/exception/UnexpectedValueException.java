package ru.valeo.jim.exception;

import java.math.BigDecimal;

public class UnexpectedValueException extends RuntimeException {
    public UnexpectedValueException(BigDecimal expected, BigDecimal actual) {
        super("Unexpected value, expected: " + expected + " but actual: " + actual);
    }
}
