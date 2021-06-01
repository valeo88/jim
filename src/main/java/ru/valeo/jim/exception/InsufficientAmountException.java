package ru.valeo.jim.exception;

public class InsufficientAmountException extends RuntimeException {
    public InsufficientAmountException(String portfolioName, String symbol) {
        super("Insufficient amount of instrument " + symbol + " in portfolio with name " + portfolioName);
    }
}
