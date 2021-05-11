package ru.valeo.jim.exception;

public class InstrumentDtoCastException extends RuntimeException {

    public InstrumentDtoCastException(Class<?> dtoClass) {
        super("Can't cast instrument to DTO " + dtoClass.getName());
    }
}
