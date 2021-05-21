package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.exception.PortfolioNotFoundException;

import java.math.BigDecimal;

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
        var portofolioDto = createTestPortfolioDto();
        portfolioService.save(portofolioDto);

        var operationDto = operationsService.addMoney(portofolioDto.getName(), value);

        assertEquals(portofolioDto.getName(), operationDto.getPortfolioName());
        assertEquals(portofolioDto.getCurrencyCode(), operationDto.getCurrencyCode());
        assertEquals(value, operationDto.getValue());
        assertNotNull(operationDto.getWhenAdd());
    }

    @Test
    void whenPortfolioNotExists_shouldThrowException() {
        assertThrows(PortfolioNotFoundException.class,
                () -> operationsService.addMoney("UNKNOWN", new BigDecimal("101.5")));
    }

    private PortfolioDto createTestPortfolioDto() {
        var dto = new PortfolioDto();
        dto.setName("Alpha");
        dto.setCurrencyCode("USD");
        dto.setAvailableMoney(BigDecimal.ZERO);
        return dto;
    }
}