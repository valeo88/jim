package ru.valeo.jim.exception;

public class CurrencyNotFoundException extends RuntimeException {

    public CurrencyNotFoundException(String code) {
        super("Not found currency with code: " + code);
    }
}
