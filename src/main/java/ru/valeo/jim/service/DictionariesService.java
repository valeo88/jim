package ru.valeo.jim.service;

import ru.valeo.jim.domain.Currency;
import ru.valeo.jim.domain.InstrumentCategory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/** Service for working with dictionaries. */
public interface DictionariesService {

    /** Get all available currencies. */
    List<Currency> getCurrencies();

    /** Save existing currency or new if not exists.
     * @param code - code (USD, EUR,...)
     * @param name - name
     * @param number - ISO number
     * @return {@link Currency} */
    Currency saveCurrency(@NotBlank String code, @NotNull String name, @NotNull String number);

    /** Delete currency.
     * @return - true if deleted. */
    boolean deleteCurrency(@NotBlank String code);

    /** Get all available instrument categories. */
    List<InstrumentCategory> getInstrumentCategories();

    /** Save existing instrument category or new if not exists.
     * @param code - code (SHR,...)
     * @param name - name
     * @return {@link InstrumentCategory} */
    InstrumentCategory saveInstrumentCategory(@NotBlank String code, @NotNull String name);

    /** Delete instrument category.
     * @return - true if deleted. */
    boolean deleteInstrumentCategory(@NotBlank String code);

    /** Get all available instrument types.*/
    List<String> getInstrumentTypes();
}
