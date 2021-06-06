package ru.valeo.jim.exception;

public class UnsupportedInstrumentTypeException extends RuntimeException {

    public UnsupportedInstrumentTypeException(String type) {
        super("Not supported instrument with type: " + type);
    }
}
