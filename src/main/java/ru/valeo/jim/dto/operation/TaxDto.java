package ru.valeo.jim.dto.operation;

import lombok.*;
import ru.valeo.jim.domain.Operation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxDto extends OperationDto {

    String currencyCode;
    @NotNull @Min(0) BigDecimal value;

    @Builder
    public TaxDto(String portfolioName, LocalDateTime whenAdd, String currencyCode, @NotNull @Min(0) BigDecimal value) {
        super(portfolioName, whenAdd);
        this.currencyCode = currencyCode;
        this.value = value;
    }

    public static TaxDto from(Operation operation) {
        var dto = new TaxDto();
        dto.setPortfolioName(operation.getPortfolio().getName());
        dto.setCurrencyCode(operation.getPortfolio().getCurrency().getCode());
        dto.setValue(operation.getTotalPrice());
        dto.setWhenAdd(operation.getWhenAdd());
        return dto;
    }

    @Override
    public String toString() {
        return "Tax operation: " +
                "portfolioName: '" + portfolioName + '\'' +
                ", amount: " + value + " " + currencyCode;
    }
}
