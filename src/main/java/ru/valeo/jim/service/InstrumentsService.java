package ru.valeo.jim.service;

import ru.valeo.jim.dto.BondDto;
import ru.valeo.jim.dto.InstrumentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/** Service for financial instruments. */
public interface InstrumentsService {

    /** Get all available instruments. */
    List<InstrumentDto> getInstruments();

    /** Save existing instrument or create new if not exists.
     * @param dto - DTO for instrument
     * @return - DTO after save. */
    InstrumentDto save(@NotNull InstrumentDto dto);

    /** Save existing bond or create new if not exists.
     * @param dto - DTO for bond
     * @return - DTO after save. */
    BondDto save(@NotNull BondDto dto);

    /** Delete instrument
     * @param symbol - code of instrument (AAPL, FXRL,...)
     * @return - true if deleted */
    boolean delete(@NotBlank String symbol);
}
