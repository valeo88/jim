package ru.valeo.jim.dto.operation;

import lombok.*;
import ru.valeo.jim.domain.Operation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawMoneyDto {

    String portfolioName;
    String currencyCode;
    LocalDateTime whenAdd;
    @NotNull @Min(0) BigDecimal value;

    public static WithdrawMoneyDto from(Operation operation) {
        var dto = new WithdrawMoneyDto();
        dto.setPortfolioName(operation.getPortfolio().getName());
        dto.setCurrencyCode(operation.getPortfolio().getCurrency().getCode());
        dto.setValue(operation.getTotalPrice());
        dto.setWhenAdd(operation.getWhenAdd());
        return dto;
    }

    @Override
    public String toString() {
        return "Withdraw money operation: " +
                "portfolioName: '" + portfolioName + '\'' +
                ", amount: " + value + "" + currencyCode;
    }
}
