package ru.valeo.jim.service;

import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.BuyInstrumentDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;

import javax.validation.constraints.NotNull;

/** Service for operations */
public interface OperationsService {

    /** Add money to portfolio. */
    AddMoneyDto addMoney(@NotNull AddMoneyDto dto);

    /** Withdraw money from portfolio. */
    WithdrawMoneyDto withdrawMoney(@NotNull WithdrawMoneyDto dto);

    /** Buy instrument. */
    BuyInstrumentDto buyInstrument(@NotNull BuyInstrumentDto dto);
}
