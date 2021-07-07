package ru.valeo.jim.cli;

import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.dto.operation.*;
import ru.valeo.jim.service.OperationsService;
import ru.valeo.jim.service.PortfolioService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.springframework.shell.standard.ShellOption.NULL;

@AllArgsConstructor
@ShellComponent
public class OperationsCommands {
    private static final String LOG_FIRST_LINE = "Processed operations (newest - top):";

    private final OperationsService operationsService;
    private final PortfolioService portfolioService;
    private final ApplicationConfig applicationConfig;

    @ShellMethod(value = "Log processed operations", key = "log-operations")
    public String logOperations(@ShellOption(defaultValue = NULL) String portfolioName) {
        var builder = new StringBuilder(LOG_FIRST_LINE);
        List<String> operations = portfolioService.getProcessedOperations(portfolioName)
                .stream()
                .map(dto -> dto.toString() + " on "
                        + applicationConfig.getOperationWhenAddFormatter().format(dto.getWhenAdd()))
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
    public String addMoney(BigDecimal amount,
                           @ShellOption(defaultValue = NULL) String whenAdd,
                           @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = AddMoneyDto.builder()
                .portfolioName(portfolioName)
                .whenAdd(parseWhenAdd(whenAdd))
                .value(amount)
                .build();
        return operationsService.addMoney(dto).toString();
    }

    @ShellMethod(value = "Withdraw money operation", key = "withdraw-money")
    public String withdrawMoney(BigDecimal amount,
                                @ShellOption(defaultValue = NULL) String whenAdd,
                                @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = WithdrawMoneyDto.builder()
                .portfolioName(portfolioName)
                .whenAdd(parseWhenAdd(whenAdd))
                .value(amount)
                .build();
        return operationsService.withdrawMoney(dto).toString();
    }

    @ShellMethod(value = "Buy instrument operation", key = "buy-instrument")
    public String buyInstrument(String symbol, BigDecimal price, Integer amount,
                                @ShellOption(defaultValue = NULL) String whenAdd,
                                @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = BuyInstrumentDto.builder()
                .portfolioName(portfolioName)
                .symbol(symbol)
                .price(price)
                .amount(amount)
                .whenAdd(parseWhenAdd(whenAdd))
                .build();
        return operationsService.buyInstrument(dto).toString();
    }

    @ShellMethod(value = "Sell instrument operation", key = "sell-instrument")
    public String sellInstrument(String symbol, BigDecimal price, Integer amount,
                                 @ShellOption(defaultValue = NULL) String whenAdd,
                                 @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = SellInstrumentDto.builder()
                .portfolioName(portfolioName)
                .symbol(symbol)
                .price(price)
                .amount(amount)
                .whenAdd(parseWhenAdd(whenAdd))
                .build();
        return operationsService.sellInstrument(dto).toString();
    }

    @ShellMethod(value = "Add dividend operation", key = "add-dividend")
    public String addDividend(String symbol, BigDecimal price, Integer amount,
                                @ShellOption(defaultValue = NULL) String whenAdd,
                                @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = DividendDto.builder()
                .portfolioName(portfolioName)
                .symbol(symbol)
                .price(price)
                .amount(amount)
                .whenAdd(parseWhenAdd(whenAdd))
                .build();
        return operationsService.dividend(dto).toString();
    }

    @ShellMethod(value = "Add coupon redemption operation", key = "add-coupon")
    public String addCoupon(String symbol, BigDecimal price, Integer amount,
                            @ShellOption(defaultValue = NULL) String whenAdd,
                            @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = CouponDto.builder()
                .portfolioName(portfolioName)
                .symbol(symbol)
                .price(price)
                .amount(amount)
                .whenAdd(parseWhenAdd(whenAdd))
                .build();
        return operationsService.coupon(dto).toString();
    }

    @ShellMethod(value = "Tax operation", key = "tax")
    public String tax(BigDecimal amount,
                      @ShellOption(defaultValue = NULL) String whenAdd,
                      @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = TaxDto.builder()
                .portfolioName(portfolioName)
                .value(amount)
                .whenAdd(parseWhenAdd(whenAdd))
                .build();
        return operationsService.tax(dto).toString();
    }

    @ShellMethod(value = "Bond redemption operation", key = "bond-redemption")
    public String bondRedemption(String symbol,
                                 @ShellOption(defaultValue = NULL) String whenAdd,
                                 @ShellOption(defaultValue = NULL) String portfolioName) {
        var dto = BondRedemptionDto.builder()
                .portfolioName(portfolioName)
                .symbol(symbol)
                .whenAdd(parseWhenAdd(whenAdd))
                .build();
        return operationsService.bondRedemption(dto).toString();
    }


    private LocalDateTime parseWhenAdd(String whenAdd) {
        return ofNullable(whenAdd)
                .filter(StringUtils::hasText)
                .map(text -> LocalDateTime.parse(text, applicationConfig.getOperationWhenAddFormatter()))
                .orElse(null);
    }
}
