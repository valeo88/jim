package ru.valeo.jim.exception;

public class PortfolioNotFoundException extends RuntimeException {

    public PortfolioNotFoundException(String name) {
        super("Not found portfolio with name: " + name);
    }
}
