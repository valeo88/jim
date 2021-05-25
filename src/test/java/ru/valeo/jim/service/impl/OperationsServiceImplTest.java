package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.exception.InsufficientMoneyException;
import ru.valeo.jim.exception.PortfolioNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OperationsServiceImplTest {

    @Autowired
    private OperationsServiceImpl operationsService;
    @Autowired
    private PortfolioServiceImpl portfolioService;

    @Test
    void whenPortfolioExists_shouldAddMoney() {
        var value = new BigDecimal("101.5");
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);

        var operationDto = operationsService.addMoney(portfolioDto.getName(), value);
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());

        assertEquals(portfolioDto.getName(), operationDto.getPortfolioName());
        assertEquals(portfolioDto.getCurrencyCode(), operationDto.getCurrencyCode());
        assertEquals(value, operationDto.getValue());
        assertNotNull(operationDto.getWhenAdd());
        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(value, reloadedPortfolioDto.get().getAvailableMoney());
    }

    @Test
    void whenPortfolioExistsAndHasSufficientMoney_shouldWithdrawMoney() {
        var balance = new BigDecimal("101.5");
        var withdrawal = new BigDecimal("11.3");
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);

        assertThrows(InsufficientMoneyException.class,
                () -> operationsService.withdrawMoney(portfolioDto.getName(), withdrawal));
    }

    @Test
    void whenPortfolioExistsAndHasNotSufficientMoney_shouldThrowError() {
        var balance = new BigDecimal("101.5");
        var withdrawal = new BigDecimal("11.3");
        var portfolioDto = createTestPortfolioDto();
        portfolioService.save(portfolioDto);

        operationsService.addMoney(portfolioDto.getName(), balance);
        var operationDto = operationsService.withdrawMoney(portfolioDto.getName(), withdrawal);
        var reloadedPortfolioDto = portfolioService.getPortfolio(portfolioDto.getName());

        assertEquals(portfolioDto.getName(), operationDto.getPortfolioName());
        assertEquals(portfolioDto.getCurrencyCode(), operationDto.getCurrencyCode());
        assertEquals(withdrawal, operationDto.getValue());
        assertNotNull(operationDto.getWhenAdd());
        assertTrue(reloadedPortfolioDto.isPresent());
        assertEquals(balance.subtract(withdrawal), reloadedPortfolioDto.get().getAvailableMoney());
    }

    @Test
    void whenPortfolioNotExists_shouldThrowException() {
        var notExistsPortfolioName = "UNKNOWN";
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.addMoney(notExistsPortfolioName, new BigDecimal("101.5")));
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.withdrawMoney(notExistsPortfolioName, new BigDecimal("101.5")));
    }

    private PortfolioDto createTestPortfolioDto() {
        var dto = new PortfolioDto();
        dto.setName(UUID.randomUUID().toString());
        dto.setCurrencyCode("USD");
        dto.setAvailableMoney(BigDecimal.ZERO);
        return dto;
    }
}