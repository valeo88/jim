package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.dto.InstrumentPriceDto;
import ru.valeo.jim.exception.InstrumentNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InstrumentsPriceServiceImplTest {

    @Autowired
    private InstrumentsPriceServiceImpl service;
    @Autowired
    private InstrumentsServiceImpl instrumentsService;

    @Test
    void shouldAddPriceIfInstrumentExists() {
        var instrumentDto = createTestInstrumentDto();
        instrumentsService.save(instrumentDto);
        var dto = new InstrumentPriceDto();
        dto.setSymbol(instrumentDto.getSymbol());
        dto.setPrice(BigDecimal.ONE);
        dto.setWhenAdd(LocalDateTime.of(2021, 12, 12, 11, 34));

        var saved = service.addPrice(dto);
        var prices = service.get(dto.getSymbol());

        assertTrue(prices.contains(saved));
    }

    @Test
    void shouldThrowExceptionIfInstrumentNotExists() {
        assertThrows(InstrumentNotFoundException.class, () -> service.get("NOT_EXISTS"));
    }


    private InstrumentDto createTestInstrumentDto() {
        var dto = new InstrumentDto();
        dto.setSymbol("XXX1");
        dto.setName("X share LLC");
        dto.setType("SHARE");
        dto.setBaseCurrencyCode("USD");
        dto.setCategoryCode("SHR");
        return dto;
    }
}