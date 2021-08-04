package ru.valeo.jim.service;

import ru.valeo.jim.dto.InstrumentPriceDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/** Working with instrument prices. */
public interface InstrumentsPriceService {

    /** Add instrument price to log. */
    InstrumentPriceDto addPrice(@NotNull InstrumentPriceDto dto);

    /** Get history by instrument. */
    List<InstrumentPriceDto> get(@NotBlank String symbol);
}
