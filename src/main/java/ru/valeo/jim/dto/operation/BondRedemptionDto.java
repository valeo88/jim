package ru.valeo.jim.dto.operation;

import lombok.*;
import ru.valeo.jim.domain.Operation;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BondRedemptionDto extends OperationDto {

    @NotBlank String symbol;
    String name;
    BigDecimal totalPrice;
    String currencyCode;

    @Builder
    public BondRedemptionDto(String portfolioName, LocalDateTime whenAdd, @NotBlank String symbol,
                             String name, BigDecimal totalPrice, String currencyCode) {
        super(portfolioName, whenAdd);
        this.symbol = symbol;
        this.name = name;
        this.totalPrice = totalPrice;
        this.currencyCode = currencyCode;
    }

    public static BondRedemptionDto from(Operation operation) {
        var dto = new BondRedemptionDto();
        dto.setPortfolioName(operation.getPortfolio().getName());
        dto.setSymbol(operation.getInstrument().getSymbol());
        dto.setName(operation.getInstrument().getName());
        dto.setTotalPrice(operation.getTotalPrice());
        dto.setWhenAdd(operation.getWhenAdd());
        dto.setCurrencyCode(operation.getPortfolio().getCurrency().getCode());
        return dto;
    }

    @Override
    public String toString() {
        return "Bond redemption operation: " +
                "portfolioName='" + portfolioName + '\'' +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", total price=" + totalPrice + " " + currencyCode;
    }

}
