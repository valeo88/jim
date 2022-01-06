package ru.valeo.jim.service;

import javax.validation.constraints.NotNull;

import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.BondRedemptionDto;
import ru.valeo.jim.dto.operation.BuyBondDto;
import ru.valeo.jim.dto.operation.BuyInstrumentDto;
import ru.valeo.jim.dto.operation.CouponDto;
import ru.valeo.jim.dto.operation.DividendDto;
import ru.valeo.jim.dto.operation.InstrumentConversionDto;
import ru.valeo.jim.dto.operation.SellBondDto;
import ru.valeo.jim.dto.operation.SellInstrumentDto;
import ru.valeo.jim.dto.operation.TaxDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;

/** Service for operations */
public interface OperationsService {

    /** Add money to portfolio. */
    AddMoneyDto addMoney(@NotNull AddMoneyDto dto);

    /** Withdraw money from portfolio. */
    WithdrawMoneyDto withdrawMoney(@NotNull WithdrawMoneyDto dto);

    /** Buy instrument. */
    BuyInstrumentDto buyInstrument(@NotNull BuyInstrumentDto dto);

    /** Buy bond. */
    BuyBondDto buyBond(@NotNull BuyBondDto dto);

    /** Sell instrument. */
    SellInstrumentDto sellInstrument(@NotNull SellInstrumentDto dto);

    /** Sell bond. */
    SellBondDto sellBond(@NotNull SellBondDto dto);

    /** Add dividend. */
    DividendDto dividend(@NotNull DividendDto dto);

    /** Add coupon. */
    CouponDto coupon(@NotNull CouponDto dto);

    /** Tax applied to portfolio. */
    TaxDto tax(@NotNull TaxDto dto);

    /** Bond redemption. */
    BondRedemptionDto bondRedemption(@NotNull BondRedemptionDto dto);

    /** Instrument conversion in portfolio. */
    InstrumentConversionDto instrumentConversion(@NotNull InstrumentConversionDto dto);
}
