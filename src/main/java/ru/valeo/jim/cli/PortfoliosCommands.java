package ru.valeo.jim.cli;

import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.valeo.jim.dto.InstrumentPositionDto;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.service.PortfolioService;

import java.util.stream.Collectors;

import static org.springframework.shell.standard.ShellOption.NULL;

@AllArgsConstructor
@ShellComponent
public class PortfoliosCommands {

    private final PortfolioService portfolioService;

    @ShellMethod(value = "Print all available portfolios", key = "portfolios")
    public String printPortfolios() {
        return portfolioService.getPortfolios().stream()
                .map(PortfolioDto::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod(value = "Update portfolio or create if not exists", key = "save-portfolio")
    public String save(String name, String currency) {
        try {
            var dto = new PortfolioDto();
            dto.setName(name);
            dto.setCurrencyCode(currency);

            return portfolioService.save(dto).toString();
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @ShellMethod(value = "Delete portfolio", key = "delete-portfolio")
    public String delete(String name) {
        if (portfolioService.delete(name)) {
            return "Deleted portfolio with name " + name;
        }
        return "Not found portfolio with name " + name;
    }

    @ShellMethod(value = "Set default portfolio", key = "set-default-portfolio")
    public String setDefault(String name) {
        try {
            portfolioService.setDefault(name);
            return "Set default portfolio name: " + name;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @ShellMethod(value = "Get default portfolio", key = "get-default-portfolio")
    public String getDefault() {
        return portfolioService.getDefault()
                .map(PortfolioDto::toString)
                .orElseGet(() -> "Default portfolio is not set!");
    }

    @ShellMethod(value = "Get instrument positions in portfolio", key = "instrument-positions")
    public String instrumentPositions(@ShellOption(defaultValue = NULL) String name) {
        return portfolioService.getInstrumentPositions(name)
                .stream()
                .filter(instrumentPositionDto -> instrumentPositionDto.getAmount() > 0)
                .map(InstrumentPositionDto::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
