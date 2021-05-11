package ru.valeo.jim.cli;

import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.valeo.jim.domain.InstrumentType;
import ru.valeo.jim.dto.BondDto;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.service.InstrumentsService;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import static org.springframework.shell.standard.ShellOption.NULL;

@AllArgsConstructor
@ShellComponent
public class InstrumentsCommands {

    private final InstrumentsService instrumentsService;

    @ShellMethod(value = "Print all available financial instruments", key = "instruments")
    public String printInstruments() {
        return instrumentsService.getInstruments().stream()
                .map(InstrumentDto::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod(value = "Update share or create if not exists", key = "save-share")
    public String saveShare(String symbol,
                                 String name,
                                 String currency,
                                 String category,
                                 @ShellOption(defaultValue = NULL) String isin) {
        try {
            var dto = new InstrumentDto();
            dto.setSymbol(symbol);
            dto.setName(name);
            dto.setBaseCurrencyCode(currency);
            dto.setType(InstrumentType.SHARE.name());
            dto.setCategoryCode(category);
            dto.setIsin(isin);

            return instrumentsService.save(dto).toString();
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @ShellMethod(value = "Update ETF or create if not exists", key = "save-etf")
    public String saveETF(String symbol,
                            String name,
                            String currency,
                            String category,
                            @ShellOption(defaultValue = NULL) String isin) {
        try {
            var dto = new InstrumentDto();
            dto.setSymbol(symbol);
            dto.setName(name);
            dto.setBaseCurrencyCode(currency);
            dto.setType(InstrumentType.ETF.name());
            dto.setCategoryCode(category);
            dto.setIsin(isin);

            return instrumentsService.save(dto).toString();
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @ShellMethod(value = "Update bond or create if not exists", key = "save-bond")
    public String saveBond(String symbol,
                         String name,
                         String currency,
                         String category,
                         BigDecimal parValue,
                         @ShellOption(defaultValue = NULL) String isin) {
        try {
            var dto = new BondDto();
            dto.setSymbol(symbol);
            dto.setName(name);
            dto.setBaseCurrencyCode(currency);
            dto.setCategoryCode(category);
            dto.setIsin(isin);
            dto.setParValue(parValue);

            return instrumentsService.save(dto).toString();
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @ShellMethod(value = "Delete instrument", key = "delete-instrument")
    public String deleteInstrument(String symbol) {
        if (instrumentsService.delete(symbol)) {
            return "Deleted instrument with code " + symbol;
        }
        return "Not found instrument with code " + symbol;
    }
}
