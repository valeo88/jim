package ru.valeo.jim.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.domain.Instrument;
import ru.valeo.jim.domain.InstrumentType;
import ru.valeo.jim.dto.BondDto;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.exception.CurrencyNotFoundException;
import ru.valeo.jim.exception.InstrumentCategoryNotFoundException;
import ru.valeo.jim.repository.CurrencyRepository;
import ru.valeo.jim.repository.InstrumentCategoryRepository;
import ru.valeo.jim.repository.InstrumentRepository;
import ru.valeo.jim.service.InstrumentsService;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class InstrumentsServiceImpl implements InstrumentsService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentCategoryRepository instrumentCategoryRepository;
    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    @Override
    public List<InstrumentDto> getInstruments() {
        return instrumentRepository.findAll().stream()
                .map(instrument -> {
                    if (InstrumentType.BOND == instrument.getType()) {
                        return BondDto.from(instrument);
                    } else {
                        return InstrumentDto.from(instrument);
                    }
                }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public InstrumentDto save(@NotNull InstrumentDto dto) {
        var currency = currencyRepository.findById(dto.getBaseCurrencyCode())
                .orElseThrow(() -> new CurrencyNotFoundException(dto.getBaseCurrencyCode()));
        var category = instrumentCategoryRepository.findById(dto.getCategoryCode())
                .orElseThrow(() -> new InstrumentCategoryNotFoundException(dto.getCategoryCode()));
        var type = InstrumentType.findByName(dto.getType());

        var instrument = instrumentRepository.findById(dto.getSymbol()).orElseGet(Instrument::new)
            .setSymbol(dto.getSymbol())
            .setName(dto.getName())
            .setBaseCurrency(currency)
            .setCategory(category)
            .setType(type)
            .setIsin(dto.getIsin());

        return InstrumentDto.from(instrumentRepository.save(instrument));
    }

    @Override
    public BondDto save(@NotNull BondDto dto) {
        var currency = currencyRepository.findById(dto.getBaseCurrencyCode())
                .orElseThrow(() -> new CurrencyNotFoundException(dto.getBaseCurrencyCode()));
        var category = instrumentCategoryRepository.findById(dto.getCategoryCode())
                .orElseThrow(() -> new InstrumentCategoryNotFoundException(dto.getCategoryCode()));

        var instrument = instrumentRepository.findById(dto.getSymbol()).orElseGet(Instrument::new)
            .setSymbol(dto.getSymbol())
            .setName(dto.getName())
            .setBaseCurrency(currency)
            .setCategory(category)
            .setType(InstrumentType.BOND)
            .setIsin(dto.getIsin())
            .setBondParValue(dto.getParValue());

        return BondDto.from(instrumentRepository.save(instrument));
    }

    @Transactional
    @Override
    public boolean delete(@NotBlank String symbol) {
        var instrumentOpt = instrumentRepository.findById(symbol);
        if (instrumentOpt.isPresent()) {
            instrumentRepository.delete(instrumentOpt.get());
            return true;
        } else {
            return false;
        }
    }
}
