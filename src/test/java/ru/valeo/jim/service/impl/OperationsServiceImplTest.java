package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.BuyInstrumentDto;
import ru.valeo.jim.dto.operation.SellInstrumentDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;
import ru.valeo.jim.exception.InstrumentNotFoundException;
import ru.valeo.jim.exception.InsufficientAmountException;
import ru.valeo.jim.exception.InsufficientMoneyException;
import ru.valeo.jim.exception.PortfolioNotFoundException;
import ru.valeo.jim.service.InstrumentsService;
import ru.valeo.jim.service.PortfolioService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OperationsServiceImplTest {

    @Autowired
    private OperationsServiceImpl operationsService;
    @Autowired
    private PortfolioService portfolioService;
    @Autowired
    private InstrumentsService instrumentsService;

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
        var incomeDto = WithdrawMoneyDto.builder()
                .portfolioName(portfolioDto.getName())
                .value(new BigDecimal("11.3"))
                .build();

        assertThrows(InsufficientMoneyException.class,
                () -> operationsService.withdrawMoney(incomeDto));
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
}