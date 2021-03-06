package ru.valeo.jim.cli;

import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.valeo.jim.dto.InstrumentPositionDto;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.service.PortfolioService;
import ru.valeo.jim.service.util.DateTimeHelper;

import static java.util.Optional.ofNullable;
import static org.springframework.shell.standard.ShellOption.NULL;

@AllArgsConstructor
@ShellComponent
public class PortfoliosCommands {
    private static final String SEPARATOR = "\n===================================\n";

    private final PortfolioService portfolioService;
    private final DateTimeHelper dateTimeHelper;

    @ShellMethod(value = "Print all available portfolios", key = "portfolios")
    public String printPortfolios() {
        return portfolioService.getPortfolios().stream()
                .map(PortfolioDto::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod(value = "Update portfolio or create if not exists", key = "save-portfolio")
    public String save(String name, String currency,
                       @ShellOption(defaultValue = NULL, help = "Comma separated list: <Category code>-<percent>,...") String targetCategories) {
        try {
            var dto = new PortfolioDto();
            dto.setName(name);
            dto.setCurrencyCode(currency);
            dto.setCategoriesTargetDistribution(targetCategories);

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

    @ShellMethod(value = "Get portfolio info", key = "portfolio-info")
    public String info(@ShellOption(defaultValue = NULL) String name,
                       @ShellOption(defaultValue = NULL) String date) {
        String portfolioInfo = ofNullable(name)
                .flatMap(portfolioService::getPortfolio)
                .map(PortfolioDto::toString)
                .orElseGet(() -> portfolioService.getDefault()
                            .map(PortfolioDto::toString)
                        .orElseGet(() -> "Portfolio not found!"));
        String instrumentPositions = portfolioService.getInstrumentPositions(name)
                .stream()
                .filter(instrumentPositionDto -> instrumentPositionDto.getAmount() > 0)
                .map(InstrumentPositionDto::toString)
                .collect(Collectors.joining(System.lineSeparator()));
        String instrumentsDistributionByAccountingPrice = "Instruments distribution (accounting price):"
                + System.lineSeparator() + portfolioService.getInstrumentsDistributionByAccoutingPrice(name).toString();
        String instrumentsDistributionByActualPrice = "Instruments distribution (actual price, accounting if not found):"
                + System.lineSeparator() + portfolioService.getInstrumentsDistributionByActualPrice(name,
                dateTimeHelper.parse(date)).toString();
        String targetInstrumentsDistribution = "Instruments distribution (target):"
                + System.lineSeparator() + portfolioService.getTargetInstrumentsDistribution(name).toString();
        return portfolioInfo + SEPARATOR
                + targetInstrumentsDistribution + SEPARATOR
                + instrumentsDistributionByAccountingPrice + SEPARATOR
                + instrumentsDistributionByActualPrice + SEPARATOR
                + instrumentPositions;
    }

    @ShellMethod(value = "Get rebalance proposition", key = "portfolio-rebalance")
    public String rebalance(@ShellOption(defaultValue = NULL) String name, boolean useAvailableMoney) {
        var dto = portfolioService.getRebalanceProposition(name, useAvailableMoney);
        return dto.getPortfolioName() + SEPARATOR
                + dto.getOperations().stream()
                    .map(op -> op.getCategoryName() + " (" + op.getCategoryCode() + ") "
                            + op.getOperation() + " - " + op.getSum())
                    .collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod(value = "Reinit portfolio. All data in portfolio will be deleted!!!", key = "reinit-portfolio")
    public String reinit(String name) {
        try {
            portfolioService.reinit(name);
            return "Performed reinitilization on portfolio: " + name;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @ShellMethod(value = "Toggle instrument from distribution", key = "toggle-exclude-instrument-from-distribution")
    public String toggleExcludeInstrumentFromDistribution(String symbol,
                                 @ShellOption(defaultValue = NULL) String portfolioName) {
        portfolioService.toggleExcludeInstrumentFromDistribution(symbol, portfolioName);
        return "Toggle excluding instrument from distribution in portfolio";
    }
}
