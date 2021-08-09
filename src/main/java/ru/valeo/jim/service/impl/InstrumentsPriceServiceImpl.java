package ru.valeo.jim.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.InstrumentPrice;
import ru.valeo.jim.domain.InstrumentType;
import ru.valeo.jim.dto.InstrumentPriceDto;
import ru.valeo.jim.exception.InstrumentNotFoundException;
import ru.valeo.jim.exception.UnsupportedInstrumentTypeException;
import ru.valeo.jim.repository.InstrumentPriceRepository;
import ru.valeo.jim.repository.InstrumentRepository;
import ru.valeo.jim.service.InstrumentsPriceService;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@AllArgsConstructor
@Service
public class InstrumentsPriceServiceImpl implements InstrumentsPriceService {

    private final InstrumentPriceRepository instrumentPriceRepository;
    private final InstrumentRepository instrumentRepository;
    private final ApplicationConfig applicationConfig;

    @Transactional
    @Override
    public InstrumentPriceDto addPrice(@NotNull InstrumentPriceDto dto) {
        var instrument = instrumentRepository.findById(dto.getSymbol())
                .orElseThrow(() -> new InstrumentNotFoundException(dto.getSymbol()));

        var data = new InstrumentPrice();
        data.setInstrument(instrument);
        if (dto.isPercent()) {
            validateInstrumentType(instrument.getType(), InstrumentType.typesWithCoupon());
            data.setPrice(instrument.getBondParValue().multiply(dto.getPrice())
                    .divide(BigDecimal.valueOf(100), applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR));
        } else {
            data.setPrice(dto.getPrice());
        }
        data.setAccumulatedCouponIncome(dto.getAccumulatedCouponIncome());
        data.setWhenAdd(nonNull(dto.getWhenAdd()) ? dto.getWhenAdd() : LocalDateTime.now());

        return InstrumentPriceDto.from(instrumentPriceRepository.save(data));
    }

    @Transactional(readOnly = true)
    @Override
    public List<InstrumentPriceDto> get(@NotBlank String symbol) {
        var instrument = instrumentRepository.findById(symbol)
                .orElseThrow(() -> new InstrumentNotFoundException(symbol));
        return instrumentPriceRepository.findByInstrument(instrument)
                .stream()
                .map(InstrumentPriceDto::from)
                .sorted(Comparator.comparing(InstrumentPriceDto::getWhenAdd))
                .collect(Collectors.toList());
    }

    private void validateInstrumentType(InstrumentType type, Set<InstrumentType> availableTypes) {
        if (!availableTypes.contains(type))
            throw new UnsupportedInstrumentTypeException(type.name());
    }
}
