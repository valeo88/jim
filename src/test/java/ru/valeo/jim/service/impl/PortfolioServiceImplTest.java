package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.dto.PortfolioDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PortfolioServiceImplTest {

    @Autowired
    private PortfolioServiceImpl service;

    @Test
    void shouldGetListOfPortfolios() {
        assertNotNull(service.getPortfolios());
    }

    @Test
    void shouldSaveNewDtoIfNotExists() {
        var dto = createTestDto();

        var saved = service.save(dto);

        assertTrue(service.getPortfolios().contains(saved));
    }

    @Test
    void shouldUpdateDtoIfExists() {
        var dto = createTestDto();
        var saved = service.save(dto);
        saved.setCurrencyCode("EUR");

        var updated = service.save(saved);

        assertTrue(service.getPortfolios().contains(saved));
    }

    @Test
    void shouldDeleteInstrumentIfExists() {
        var dto = createTestDto();
        var saved = service.save(dto);

        service.delete(saved.getName());

        assertFalse(service.getPortfolios().contains(saved));
    }

    private PortfolioDto createTestDto() {
        var dto = new PortfolioDto();
        dto.setName("Alpha");
        dto.setCurrencyCode("USD");
        dto.setAvailableMoney(BigDecimal.ZERO);
        return dto;
    }

}