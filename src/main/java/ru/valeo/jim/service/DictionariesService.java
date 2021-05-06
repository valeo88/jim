package ru.valeo.jim.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.domain.Currency;
import ru.valeo.jim.domain.InstrumentCategory;
import ru.valeo.jim.domain.InstrumentType;
import ru.valeo.jim.repository.CurrencyRepository;
import ru.valeo.jim.repository.InstrumentCategoryRepository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class DictionariesService {

    private final CurrencyRepository currencyRepository;
    private final InstrumentCategoryRepository instrumentCategoryRepository;

    @Transactional(readOnly = true)
    public List<Currency> getCurrencies() {
        return currencyRepository.findAll();
    }

    @Transactional
    public Currency saveCurrency(@NotBlank String code, @NotNull String name, @NotNull String number) {
        var currency = currencyRepository.findById(code).orElse(new Currency());
        currency.setCode(code);
        currency.setName(name);
        currency.setNumber(number);
        return currencyRepository.save(currency);
    }

    @Transactional
    public boolean deleteCurrency(@NotBlank String code) {
        var currencyOpt = currencyRepository.findById(code);
        if (currencyOpt.isPresent()) {
            currencyRepository.delete(currencyOpt.get());
            return true;
        } else {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<InstrumentCategory> getInstrumentCategories() {
        return instrumentCategoryRepository.findAll();
    }

    @Transactional
    public InstrumentCategory saveInstrumentCategory(@NotBlank String code, @NotNull String name) {
        var category = instrumentCategoryRepository.findById(code).orElse(new InstrumentCategory());
        category.setCode(code);
        category.setName(name);
        return instrumentCategoryRepository.save(category);
    }

    @Transactional
    public boolean deleteInstrumentCategory(@NotBlank String code) {
        var instrumentCategoryOpt = instrumentCategoryRepository.findById(code);
        if (instrumentCategoryOpt.isPresent()) {
            instrumentCategoryRepository.delete(instrumentCategoryOpt.get());
            return true;
        } else {
            return false;
        }
    }

    public List<String> getInstrumentTypes() {
        return Arrays.stream(InstrumentType.values()).map(Enum::name).collect(Collectors.toList());
    }
}
