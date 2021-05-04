package ru.valeo.jim.cli;

import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.valeo.jim.domain.Currency;
import ru.valeo.jim.domain.InstrumentCategory;
import ru.valeo.jim.service.DictionariesService;

import java.util.stream.Collectors;

@AllArgsConstructor
@ShellComponent
public class DictionariesCommands {

    private final DictionariesService dictionariesService;

    @ShellMethod(value = "Print all available currencies", key = "currencies")
    public String printCurrencies() {
        return dictionariesService.getCurrencies().stream()
                .map(Currency::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod(value = "Update currency or create if not exists", key = "save-currency")
    public String saveCurrency(String code, String name, String number) {
        return dictionariesService.saveCurrency(code, name, number).toString();
    }

    @ShellMethod(value = "Delete currency", key = "delete-currency")
    public String deleteCurrency(String code) {
        if (dictionariesService.deleteCurrency(code)) {
            return "Deleted currency with code " + code;
        }
        return "Not found currency with code " + code;
    }

    @ShellMethod(value = "Print all available instrument categories", key = "categories")
    public String printInstrumentCategories() {
        return dictionariesService.getInstrumentCategories().stream()
                .map(InstrumentCategory::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod(value = "Update instrument category or create if not exists", key = "save-category")
    public String saveInstrumentCategory(String code, String name) {
        return dictionariesService.saveInstrumentCategory(code, name).toString();
    }

    @ShellMethod(value = "Delete instrument category", key = "delete-category")
    public String deleteInstrumentCategory(String code) {
        if (dictionariesService.deleteInstrumentCategory(code)) {
            return "Deleted instrument category with code " + code;
        }
        return "Not found instrument category with code " + code;
    }

    @ShellMethod(value = "Print all available instrument types", key = "instrument-types")
    public String printInstrumentTypes() {
        return dictionariesService.getInstrumentTypes().stream()
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
