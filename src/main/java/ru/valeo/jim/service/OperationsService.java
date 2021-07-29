package ru.valeo.jim.service;

import ru.valeo.jim.dto.operation.*;

import javax.validation.constraints.NotNull;

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
}
