package ru.valeo.jim.exception;

public class InstrumentTypeNotFoundException extends RuntimeException {

    public InstrumentTypeNotFoundException(String name) {
        super("Not found instrument type with name: " + name);
    }
}
