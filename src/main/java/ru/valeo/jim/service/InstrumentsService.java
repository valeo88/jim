package ru.valeo.jim.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.domain.Instrument;
import ru.valeo.jim.domain.InstrumentType;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.exception.CurrencyNotFoundException;
import ru.valeo.jim.exception.InstrumentCategoryNotFoundException;
import ru.valeo.jim.repository.CurrencyRepository;
import ru.valeo.jim.repository.InstrumentCategoryRepository;
import ru.valeo.jim.repository.InstrumentRepository;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class InstrumentsService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentCategoryRepository instrumentCategoryRepository;
    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    public List<InstrumentDto> getInstruments() {
        return instrumentRepository.findAll().stream().map(InstrumentDto::from).collect(Collectors.toList());
    }

    @Transactional
    public InstrumentDto save(@NotBlank String symbol, @NotBlank String name, @NotBlank String baseCurrencyCode,
                              @NotBlank String typeName, @NotBlank String categoryCode, String isin) {
        var currency = currencyRepository.findById(baseCurrencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException(baseCurrencyCode));
        var category = instrumentCategoryRepository.findById(categoryCode)
                .orElseThrow(() -> new InstrumentCategoryNotFoundException(categoryCode));
        var type = InstrumentType.findByName(typeName);

        var instrument = instrumentRepository.findById(symbol).orElseGet(Instrument::new);
        instrument.setSymbol(symbol);
        instrument.setName(name);
        instrument.setBaseCurrency(currency);
        instrument.setCategory(category);
        instrument.setType(type);
        instrument.setIsin(isin);

        return InstrumentDto.from(instrumentRepository.save(instrument));
    }
}
