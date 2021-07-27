package ru.valeo.jim.dto.operation;

import lombok.*;
import ru.valeo.jim.domain.Operation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyBondDto extends OperationDto {

    @NotBlank String symbol;
    String name;
    @NotNull @Min(1) Integer amount;
    @NotNull @Min(0) BigDecimal percent;
    @NotNull @Min(0) BigDecimal accumulatedCouponIncome;
    String currencyCode;

    @Builder
    public BuyBondDto(String portfolioName, LocalDateTime whenAdd, @NotBlank String symbol, String name,
                      @NotNull @Min(1) Integer amount, @NotNull @Min(0) BigDecimal percent,
                      @NotNull @Min(0) BigDecimal accumulatedCouponIncome, String currencyCode) {
        super(portfolioName, whenAdd);
        this.symbol = symbol;
        this.name = name;
        this.amount = amount;
        this.percent = percent;
        this.accumulatedCouponIncome = accumulatedCouponIncome;
        this.currencyCode = currencyCode;
    }

    public static BuyBondDto from(Operation operation) {
        var dto = new BuyBondDto();
        dto.setPortfolioName(operation.getPortfolio().getName());
        dto.setSymbol(operation.getInstrument().getSymbol());
        dto.setName(operation.getInstrument().getName());
        dto.setAmount(operation.getAmount());
        dto.setPercent(operation.getPercent());
        dto.setAccumulatedCouponIncome(operation.getAccumulatedCouponIncome());
        dto.setWhenAdd(operation.getWhenAdd());
        dto.setCurrencyCode(operation.getPortfolio().getCurrency().getCode());
        return dto;
    }

    @Override
    public String toString() {
        return "Buy bond operation: " +
                "portfolioName='" + portfolioName + '\'' +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", percent=" + percent +
                ", accumulatedCouponIncome=" + accumulatedCouponIncome +
                ", currency=" + currencyCode;
    }
}
