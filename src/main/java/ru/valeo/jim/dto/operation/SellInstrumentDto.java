package ru.valeo.jim.dto.operation;

import lombok.*;
import ru.valeo.jim.domain.Operation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellInstrumentDto {

    String portfolioName;
    @NotBlank String symbol;
    String name;
    @NotNull @Min(1) Integer amount;
    @NotNull @Min(0) BigDecimal price;
    LocalDateTime whenAdd;
    String currencyCode;

    public static SellInstrumentDto from(Operation operation) {
        var dto = new SellInstrumentDto();
        dto.setPortfolioName(operation.getPortfolio().getName());
        dto.setSymbol(operation.getInstrument().getSymbol());
        dto.setName(operation.getInstrument().getName());
        dto.setAmount(operation.getAmount());
        dto.setPrice(operation.getPrice());
        dto.setWhenAdd(operation.getWhenAdd());
        dto.setCurrencyCode(operation.getPortfolio().getCurrency().getCode());
        return dto;
    }

    @Override
    public String toString() {
        return "Sell instrument operation: " +
                "portfolioName='" + portfolioName + '\'' +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", price=" + price + "" + currencyCode;
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(amount));
    }
}