package ru.valeo.jim.exception;

public class InstrumentNotFoundException extends RuntimeException {

    public InstrumentNotFoundException(String symbol) {
        super("Not found instrument with symbol: " + symbol);
    }
}
