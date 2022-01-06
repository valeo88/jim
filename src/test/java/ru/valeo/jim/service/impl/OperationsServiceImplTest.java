package ru.valeo.jim.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.InstrumentType;
import ru.valeo.jim.dto.BondDto;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.BondRedemptionDto;
import ru.valeo.jim.dto.operation.BuyBondDto;
import ru.valeo.jim.dto.operation.BuyInstrumentDto;
import ru.valeo.jim.dto.operation.CouponDto;
import ru.valeo.jim.dto.operation.DividendDto;
import ru.valeo.jim.dto.operation.InstrumentConversionDto;
import ru.valeo.jim.dto.operation.SellInstrumentDto;
import ru.valeo.jim.dto.operation.TaxDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;
import ru.valeo.jim.exception.InstrumentNotFoundException;
import ru.valeo.jim.exception.InsufficientAmountException;
import ru.valeo.jim.exception.InsufficientMoneyException;
import ru.valeo.jim.exception.PortfolioNotFoundException;
import ru.valeo.jim.exception.UnsupportedInstrumentTypeException;
import ru.valeo.jim.service.InstrumentsService;
import ru.valeo.jim.service.PortfolioService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OperationsServiceImplTest {

    @Autowired
    private OperationsServiceImpl operationsService;
    @Autowired
    private PortfolioService portfolioService;
    @Autowired
    private InstrumentsService instrumentsService;
    @Autowired
    private ApplicationConfig applicationConfig;

    @Test
    void whenPortfolioExists_shouldAddMoney() {
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var incomeDto = AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("101.5"))
                .build();

        var operationDto = operationsService.addMoney(incomeDto);
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());

        assertEquals(portfolioDto.getName(), operationDto.getPortfolioName());
        assertEquals(portfolioDto.getCurrencyCode(), operationDto.getCurrencyCode());
        assertEquals(incomeDto.getValue(), operationDto.getValue());
        assertNotNull(operationDto.getWhenAdd());
        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(incomeDto.getValue(), reloadedPortfolioDto.get().getAvailableMoney());
    }

    @Test
    void whenPortfolioExistsAndHasNotSufficientMoney_shouldThrowError() {
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var withdrawMoneyDto = WithdrawMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("11.3"))
                .build();
        var taxDto = TaxDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("1"))
                .build();

        assertThrows(InsufficientMoneyException.class,
                () -> operationsService.withdrawMoney(withdrawMoneyDto));
        assertThrows(InsufficientMoneyException.class,
                () -> operationsService.tax(taxDto));
    }

    @Test
    void whenPortfolioExistsAndHasSufficientMoney_shouldWithdrawMoney() {
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);

        var addMoneyDto = AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("101.5"))
                .build();
        var withdrawMoneyDto = WithdrawMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("11.3"))
                .build();

        operationsService.addMoney(addMoneyDto);
        var operationDto = operationsService.withdrawMoney(withdrawMoneyDto);
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());

        assertEquals(portfolioDto.getName(), operationDto.getPortfolioName());
        assertEquals(portfolioDto.getCurrencyCode(), operationDto.getCurrencyCode());
        assertEquals(withdrawMoneyDto.getValue(), operationDto.getValue());
        assertNotNull(operationDto.getWhenAdd());
        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue().subtract(withdrawMoneyDto.getValue()), reloadedPortfolioDto.get().getAvailableMoney());
    }

    @Test
    void whenPortfolioAndInstrumentExistsAndHasSufficientMoney_shouldBuyInstrument() {
        // create test portfolio with sufficient money
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        operationsService.addMoney(AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("100"))
                .build());
        // create test instrument
        var instrumentDto = createInstrumentDto();
        instrumentsService.save(instrumentDto);
        var buyInstrumentDto = BuyInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("15"))
                .build();

        var operationDto = operationsService.buyInstrument(buyInstrumentDto);
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());
        var positions = portfolioService.getInstrumentPositions(portfolioDto.getName());

        assertEquals(portfolioDto.getName(), operationDto.getPortfolioName());
        assertEquals(portfolioDto.getCurrencyCode(), operationDto.getCurrencyCode());
        assertEquals(buyInstrumentDto.getTotalPrice(), operationDto.getTotalPrice());
        assertNotNull(operationDto.getWhenAdd());
        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(new BigDecimal("55"), reloadedPortfolioDto.get().getAvailableMoney());
        assertFalse(positions.isEmpty());
        assertTrue(positions.stream()
                .filter(instrumentPositionDto -> instrumentPositionDto.getSymbol().equals(operationDto.getSymbol()))
                .filter(instrumentPositionDto -> instrumentPositionDto.getAmount().equals(operationDto.getAmount()))
                .anyMatch(instrumentPositionDto -> instrumentPositionDto.getAccountingPrice().equals(operationDto.getPrice())));
    }

    @Test
    void whenBuyAndSellOperationsPerformed_shouldHaveCorrectAccountingPrice() {
        // create test portfolio with sufficient money
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var addMoneyDto = operationsService.addMoney(AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("1000"))
                .build());
        // create test instrument
        var instrumentDto = createInstrumentDto();
        instrumentsService.save(instrumentDto);

        var firstBuyOperation = operationsService.buyInstrument(BuyInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("15"))
                .build());
        // try sell more than have
        assertThrows(InsufficientAmountException.class,
                () -> operationsService.sellInstrument(SellInstrumentDto.builder()
                        .portfolioName(portfolioDto.getName())
                        .symbol(instrumentDto.getSymbol())
                        .amount(10)
                        .price(new BigDecimal("15"))
                        .build()));

        var secondBuyOperation = operationsService.buyInstrument(BuyInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(5)
                .price(new BigDecimal("20"))
                .build());
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());
        var positions = portfolioService.getInstrumentPositions(portfolioDto.getName());

        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue().subtract(firstBuyOperation.getTotalPrice())
                .subtract(secondBuyOperation.getTotalPrice()),
                reloadedPortfolioDto.get().getAvailableMoney());
        assertFalse(positions.isEmpty());
        assertTrue(positions.stream()
                .filter(instrumentPositionDto -> instrumentPositionDto.getSymbol().equals(firstBuyOperation.getSymbol()))
                .filter(instrumentPositionDto -> instrumentPositionDto.getAmount()
                        .equals(firstBuyOperation.getAmount() + secondBuyOperation.getAmount()))
                .anyMatch(instrumentPositionDto -> instrumentPositionDto.getAccountingPrice()
                        .equals(new BigDecimal("18.125"))));

        var firstSellOperation = operationsService.sellInstrument(SellInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("25"))
                .build());
        reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());
        positions = portfolioService.getInstrumentPositions(portfolioDto.getName());

        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue().subtract(firstBuyOperation.getTotalPrice())
                        .subtract(secondBuyOperation.getTotalPrice())
                        .add(firstSellOperation.getTotalPrice()),
                reloadedPortfolioDto.get().getAvailableMoney());
        assertFalse(positions.isEmpty());
        assertTrue(positions.stream()
                .filter(instrumentPositionDto -> instrumentPositionDto.getSymbol().equals(firstBuyOperation.getSymbol()))
                .filter(instrumentPositionDto -> instrumentPositionDto.getAmount()
                        .equals(firstBuyOperation.getAmount() + secondBuyOperation.getAmount()
                                - firstSellOperation.getAmount()))
                .anyMatch(instrumentPositionDto -> instrumentPositionDto.getAccountingPrice()
                        .equals(new BigDecimal("14.000"))));

        // sell all available amount of instrument
        var secondSellOperation = operationsService.sellInstrument(SellInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(5)
                .price(new BigDecimal("22"))
                .build());
        reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());
        positions = portfolioService.getInstrumentPositions(portfolioDto.getName());

        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue().subtract(firstBuyOperation.getTotalPrice())
                        .subtract(secondBuyOperation.getTotalPrice())
                        .add(firstSellOperation.getTotalPrice())
                        .add(secondSellOperation.getTotalPrice()),
                reloadedPortfolioDto.get().getAvailableMoney());
        assertFalse(positions.isEmpty());
        assertTrue(positions.stream()
                .filter(instrumentPositionDto -> instrumentPositionDto.getSymbol().equals(firstBuyOperation.getSymbol()))
                .filter(instrumentPositionDto -> instrumentPositionDto.getAmount()
                        .equals(firstBuyOperation.getAmount() + secondBuyOperation.getAmount()
                                - firstSellOperation.getAmount() - secondSellOperation.getAmount()))
                .anyMatch(instrumentPositionDto -> instrumentPositionDto.getAccountingPrice()
                        .equals(BigDecimal.ZERO)), "should have accounting price equals to ZERO");
    }

    @Test
    void whenHasShareInPortfolio_shouldPerformDividendOperation() {
        // create test portfolio with sufficient money
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var addMoneyDto = operationsService.addMoney(AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("1000"))
                .build());
        // create test share
        var instrumentDto = createInstrumentDto();
        instrumentDto.setType(InstrumentType.SHARE.name());
        instrumentsService.save(instrumentDto);

        var buyOperation = operationsService.buyInstrument(BuyInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("15"))
                .build());
        var dividendOperation = operationsService.dividend(DividendDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("1.5"))
                .build());
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());

        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue().subtract(buyOperation.getTotalPrice())
                        .add(dividendOperation.getTotalPrice()),
                reloadedPortfolioDto.get().getAvailableMoney());
    }

    @Test
    void whenHasShareInPortfolio_shouldPerformConversionOperation() {
        // create test portfolio with sufficient money
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var addMoneyDto = operationsService.addMoney(AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("1000"))
                .build());
        // create test share
        var instrumentDto = createInstrumentDto();
        instrumentDto.setType(InstrumentType.SHARE.name());
        instrumentsService.save(instrumentDto);

        var buyOperation = operationsService.buyInstrument(BuyInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("15"))
                .build());
        var conversionOperation = operationsService.instrumentConversion(InstrumentConversionDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(instrumentDto.getSymbol())
                .newAmount(30)
                .build());
        var instrumentPositions = portfolioService.getInstrumentPositions(portfolioDto.getName());

        assertFalse(instrumentPositions.isEmpty());
        assertEquals(conversionOperation.getNewAmount(), instrumentPositions.get(0).getAmount());
        assertEquals(new BigDecimal("1.500"), instrumentPositions.get(0).getAccountingPrice());
    }

    @Test
    void whenHasBondInPortfolio_shouldPerformCouponOperation() {
        // create test portfolio with sufficient money
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var addMoneyDto = operationsService.addMoney(AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("1000"))
                .build());
        // create test bond
        var bondDto = createBondDto();
        instrumentsService.save(bondDto);

        var buyOperation = operationsService.buyBond(BuyBondDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(bondDto.getSymbol())
                .amount(3)
                .percent(new BigDecimal("101.1"))
                .accumulatedCouponIncome(new BigDecimal("3.2"))
                .build());
        var couponOperation = operationsService.coupon(CouponDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(bondDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("1.5"))
                .build());
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());

        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue()
                        .subtract(buyOperation.getAccumulatedCouponIncome())
                        .subtract(bondDto.getParValue()
                                .multiply(BigDecimal.valueOf(buyOperation.getAmount()))
                                .multiply(buyOperation.getPercent()
                                        .divide(BigDecimal.valueOf(100), applicationConfig.getBigdecimalOperationsScale(),
                                                RoundingMode.FLOOR)))
                        .add(couponOperation.getTotalPrice())
                        .setScale(applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR),
                reloadedPortfolioDto.get().getAvailableMoney());
    }

    @Test
    void whenHasBondInPortfolio_shouldPerformBondRedemptionOperation() {
        // create test portfolio with sufficient money
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var addMoneyDto = operationsService.addMoney(AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("1000"))
                .build());
        // create test bond
        var bondDto = createBondDto();
        instrumentsService.save(bondDto);

        var buyOperation = operationsService.buyBond(BuyBondDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(bondDto.getSymbol())
                .amount(3)
                .percent(new BigDecimal("101.12"))
                .accumulatedCouponIncome(new BigDecimal("3.2"))
                .build());
        var accumulatedCouponIncomeOnRedemption = new BigDecimal("5.5");
        var bondRedemptionOperation = operationsService.bondRedemption(BondRedemptionDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(bondDto.getSymbol())
                .accumulatedCouponIncome(accumulatedCouponIncomeOnRedemption)
                .build());
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());
        var positions = portfolioService.getInstrumentPositions(portfolioDto.getName());

        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue().subtract(buyOperation.getAccumulatedCouponIncome())
                    .subtract(bondDto.getParValue()
                            .multiply(BigDecimal.valueOf(buyOperation.getAmount()))
                            .multiply(buyOperation.getPercent())
                            .divide(BigDecimal.valueOf(100), applicationConfig.getBigdecimalOperationsScale(),
                                    RoundingMode.FLOOR))
                        .add(bondRedemptionOperation.getTotalPrice())
                        .add(accumulatedCouponIncomeOnRedemption)
                        .setScale(applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR),
                reloadedPortfolioDto.get().getAvailableMoney());
        assertTrue(positions.stream()
                .filter(instrumentPositionDto -> instrumentPositionDto.getSymbol().equals(bondDto.getSymbol()))
                .filter(instrumentPositionDto -> instrumentPositionDto.getAmount().equals(0))
                .anyMatch(instrumentPositionDto -> instrumentPositionDto.getAccountingPrice()
                        .equals(BigDecimal.ZERO)), "should have accounting price equals to ZERO");
    }

    @Test
    void whenPortfolioExistsAndHasSufficientMoney_shouldApplyTax() {
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);

        var addMoneyDto = AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("101.5"))
                .build();
        var taxDto = TaxDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("11.3"))
                .build();

        operationsService.addMoney(addMoneyDto);
        var operationDto = operationsService.tax(taxDto);
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());

        assertEquals(portfolioDto.getName(), operationDto.getPortfolioName());
        assertEquals(portfolioDto.getCurrencyCode(), operationDto.getCurrencyCode());
        assertEquals(taxDto.getValue(), operationDto.getValue());
        assertNotNull(operationDto.getWhenAdd());
        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(addMoneyDto.getValue().subtract(taxDto.getValue()), reloadedPortfolioDto.get().getAvailableMoney());
    }

    @Test
    void whenOperationNotSupportedForInstrumentType_shouldThrowException() {
        // create test portfolio with sufficient money
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);
        var addMoneyDto = operationsService.addMoney(AddMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("1000"))
                .build());
        // create test bond
        var bondDto = createBondDto();
        bondDto.setSymbol("BOND_1");
        bondDto.setType(InstrumentType.BOND.name());
        instrumentsService.save(bondDto);
        // create test share
        var shareDto = createInstrumentDto();
        shareDto.setSymbol("SHARE_1");
        shareDto.setType(InstrumentType.SHARE.name());
        instrumentsService.save(shareDto);

        operationsService.buyBond(BuyBondDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(bondDto.getSymbol())
                .amount(10)
                .percent(new BigDecimal("99.1"))
                .accumulatedCouponIncome(new BigDecimal("1"))
                .build());
        operationsService.buyInstrument(BuyInstrumentDto.builder()
                .portfolioName(portfolioDto.getName())
                .symbol(shareDto.getSymbol())
                .amount(3)
                .price(new BigDecimal("15"))
                .build());

        assertThrows(UnsupportedInstrumentTypeException.class,
                () -> operationsService.dividend(DividendDto.builder()
                        .portfolioName(portfolioDto.getName())
                        .symbol(bondDto.getSymbol())
                        .amount(3)
                        .price(new BigDecimal("15"))
                        .build()));
        assertThrows(UnsupportedInstrumentTypeException.class,
                () -> operationsService.coupon(CouponDto.builder()
                        .portfolioName(portfolioDto.getName())
                        .symbol(shareDto.getSymbol())
                        .amount(3)
                        .price(new BigDecimal("15"))
                        .build()));
        assertThrows(UnsupportedInstrumentTypeException.class,
                () -> operationsService.bondRedemption(BondRedemptionDto.builder()
                        .portfolioName(portfolioDto.getName())
                        .symbol(shareDto.getSymbol())
                        .build()));
        assertThrows(UnsupportedInstrumentTypeException.class,
                () -> operationsService.buyInstrument(BuyInstrumentDto.builder()
                        .portfolioName(portfolioDto.getName())
                        .symbol(bondDto.getSymbol())
                        .amount(10)
                        .price(new BigDecimal("10"))
                        .build()));
    }

    @Test
    void whenPortfolioNotExists_shouldThrowException() {
        var notExistsPortfolioName = "UNKNOWN";
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.addMoney(AddMoneyDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .value(new BigDecimal("101.5"))
                        .build()));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.withdrawMoney(WithdrawMoneyDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .value(new BigDecimal("101.5"))
                        .build()));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.buyInstrument(BuyInstrumentDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .symbol("X")
                        .amount(3)
                        .price(new BigDecimal("15"))
                        .build()));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.sellInstrument(SellInstrumentDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .symbol("X")
                        .amount(3)
                        .price(new BigDecimal("15"))
                        .build()));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.coupon(CouponDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .symbol("X")
                        .amount(3)
                        .price(new BigDecimal("1.1"))
                        .build()));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.dividend(DividendDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .symbol("X")
                        .amount(3)
                        .price(new BigDecimal("3"))
                        .build()));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.tax(TaxDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .value(new BigDecimal("3"))
                        .build()));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.bondRedemption(BondRedemptionDto.builder()
                        .portfolioName(notExistsPortfolioName)
                        .symbol("X")
                        .build()));
    }

        @Test
        void whenPortfolioExistsButInstrumentNotExists_shouldThrowException() {
            var portfolioDto = createTestPortfolioDto();
            portfolioService.save(portfolioDto);

            assertThrows(InstrumentNotFoundException.class,
                    () -> operationsService.buyInstrument(BuyInstrumentDto.builder()
                            .portfolioName(portfolioDto.getName())
                            .symbol("X")
                            .amount(3)
                            .price(new BigDecimal("15"))
                            .build()));
            assertThrows(InstrumentNotFoundException.class,
                    () -> operationsService.sellInstrument(SellInstrumentDto.builder()
                            .portfolioName(portfolioDto.getName())
                            .symbol("X")
                            .amount(3)
                            .price(new BigDecimal("15"))
                            .build()));
            assertThrows(InstrumentNotFoundException.class,
                    () -> operationsService.coupon(CouponDto.builder()
                            .portfolioName(portfolioDto.getName())
                            .symbol("X")
                            .amount(3)
                            .price(new BigDecimal("1.1"))
                            .build()));
            assertThrows(InstrumentNotFoundException.class,
                    () -> operationsService.dividend(DividendDto.builder()
                            .portfolioName(portfolioDto.getName())
                            .symbol("X")
                            .amount(3)
                            .price(new BigDecimal("3"))
                            .build()));
            assertThrows(InstrumentNotFoundException.class,
                    () -> operationsService.bondRedemption(BondRedemptionDto.builder()
                            .portfolioName(portfolioDto.getName())
                            .symbol("X")
                            .build()));

        }

    private PortfolioDto createTestPortfolioDto() {
        var dto = new PortfolioDto();
        dto.setName(UUID.randomUUID().toString());
        dto.setCurrencyCode("USD");
        dto.setAvailableMoney(BigDecimal.ZERO);
        return dto;
    }

    private InstrumentDto createInstrumentDto() {
        var dto = new InstrumentDto();
        dto.setSymbol("XXX");
        dto.setName("X share LLC");
        dto.setType("SHARE");
        dto.setBaseCurrencyCode("USD");
        dto.setCategoryCode("SHR");
        return dto;
    }

    private BondDto createBondDto() {
        var dto = new BondDto();
        dto.setSymbol("BND1");
        dto.setName("X bond LLC");
        dto.setType("BOND");
        dto.setBaseCurrencyCode("USD");
        dto.setCategoryCode("GOVB");
        dto.setParValue(new BigDecimal("10.0"));
        return dto;
    }
}