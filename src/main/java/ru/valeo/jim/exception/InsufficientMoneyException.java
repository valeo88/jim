package ru.valeo.jim.exception;

public class InsufficientMoneyException extends RuntimeException {
    public InsufficientMoneyException(String name) {
        super("Insufficient money in portfolio with name: " + name);
    }
}
