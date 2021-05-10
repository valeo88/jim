package ru.valeo.jim.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.service.impl.InstrumentsServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InstrumentsServiceImplTest {

    @Autowired
    private InstrumentsServiceImpl service;

    @Test
    void shouldGetListOfInstruments() {
        assertNotNull(service.getInstruments());
    }

    @Test
    void shouldSaveNewDtoIfNotExists() {
        var dto = createTestDto();

        var saved = service.save(dto);

        assertTrue(service.getInstruments().contains(saved));
    }

    @Test
    void shouldUpdateDtoIfExists() {
        var dto = createTestDto();
        var saved = service.save(dto);
        saved.setType("BOND");

        var updated = service.save(saved);

        assertTrue(service.getInstruments().contains(saved));
    }

    @Test
    void shouldDeleteInstrumentIfExists() {
        var dto = createTestDto();
        var saved = service.save(dto);

        service.delete(saved.getSymbol());

        assertFalse(service.getInstruments().contains(saved));
    }

    private InstrumentDto createTestDto() {
        var dto = new InstrumentDto();
        dto.setSymbol("XXX");
        dto.setName("X share LLC");
        dto.setType("SHARE");
        dto.setBaseCurrencyCode("USD");
        dto.setCategoryCode("SHR");
        return dto;
    }
}