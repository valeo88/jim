package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.exception.PortfolioNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PortfolioServiceImplTest {

    @Autowired
    private PortfolioServiceImpl service;
    @Autowired
    private ApplicationConfig applicationConfig;

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
    void shouldDeletePortfolioIfExists() {
        var dto = createTestDto();
        var saved = service.save(dto);

        service.delete(saved.getName());

        assertFalse(service.getPortfolios().contains(saved));
    }

    @Test
    void shouldSetDefaultPortfolioIfExists() {
        var dto = createTestDto();
        var saved = service.save(dto);

        service.setDefault(saved.getName());

        assertEquals(saved.getName(), applicationConfig.getDefaultPortfolioName());
    }

    @Test
    void shouldThrowExceptionOnSetDefaultPortfolioIfNotExists() {
        assertThrows(PortfolioNotFoundException.class, () -> service.setDefault("UNKNOWN"));
    }

    private PortfolioDto createTestDto() {
        var dto = new PortfolioDto();
        dto.setName(UUID.randomUUID().toString());
        dto.setCurrencyCode("USD");
        dto.setAvailableMoney(BigDecimal.ZERO);
        return dto;
    }

}