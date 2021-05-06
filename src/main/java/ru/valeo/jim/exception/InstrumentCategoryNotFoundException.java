package ru.valeo.jim.exception;

public class InstrumentCategoryNotFoundException extends RuntimeException {

    public InstrumentCategoryNotFoundException(String code) {
        super("Not found instrument category with code: " + code);
    }
}
