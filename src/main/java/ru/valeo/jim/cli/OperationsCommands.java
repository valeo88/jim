package ru.valeo.jim.cli;

import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;
import ru.valeo.jim.service.OperationsService;
import ru.valeo.jim.service.PortfolioService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.shell.standard.ShellOption.NULL;

@AllArgsConstructor
@ShellComponent
public class OperationsCommands {
    private static final String LOG_FIRST_LINE = "Processed operations (newest - top):";

    private final OperationsService operationsService;
    private final PortfolioService portfolioService;

    @ShellMethod(value = "Log processed operations", key = "log-operations")
    public String logOperations(@ShellOption(defaultValue = NULL) String portfolioName) {
        var builder = new StringBuilder(LOG_FIRST_LINE);
        List<String> operations = portfolioService.getProcessedOperations(portfolioName)
                .stream()
                .map(Objects::toString)
                .collect(Collectors.toList());
        for (var i = 0; i < operations.size(); i++) {
            builder.append(System.lineSeparator());
            builder.append(operations.size() - i);
            builder.append(". ");
            builder.append(operations.get(i));
        }
        return builder.toString();
    }

    @ShellMethod(value = "Add money operation", key = "add-money")
    public String addMoney(BigDecimal amount, @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = AddMoneyDto.builder()
                .portfolioName(portfolioName)
                .value(amount)
                .build();
        return operationsService.addMoney(dto).toString();
    }

    @ShellMethod(value = "Withdraw money operation", key = "withdraw-money")
    public String withdrawMoney(BigDecimal amount, @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = WithdrawMoneyDto.builder()
                .portfolioName(portfolioName)
                .value(amount)
                .build();
        return operationsService.withdrawMoney(dto).toString();
    }
}
