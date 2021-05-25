package ru.valeo.jim.service;

import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Service for operations */
public interface OperationsService {

    /** Add money to portfolio. */
    AddMoneyDto addMoney(String portfolioName, @NotNull @Min(0) BigDecimal value);

    /** Withdraw money from portfolio. */
    WithdrawMoneyDto withdrawMoney(String portfolioName, @NotNull @Min(0) BigDecimal value);
}
