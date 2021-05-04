package ru.valeo.jim.domain;

public enum OperationType {
    /** Increase amount of money in portfolio account. */
    ADD_MONEY,

    /** Decrease amount of money in portfolio account. */
    WITHDRAW_MONEY,

    /** Buy financial instrument. */
    BUY,

    /** Sell financial instrument. */
    SELL,

    /** Dividend given from share. */
    DIVIDEND,

    /** Coupon given from bond. */
    COUPON,

    /** Fees payed to broker, government, ... */
    TAX;
}
