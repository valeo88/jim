package ru.valeo.jim.exception;

public class InstrumentPositionNotFoundException extends RuntimeException {
    public InstrumentPositionNotFoundException(String portfolioName, String symbol) {
        super("Not found position of instrument " + symbol + " in portfolio with name " + portfolioName);
    }
}
