package ru.valeo.jim.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.Portfolio;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class PortfolioDto {

    @NotBlank protected String name;
    @NotBlank protected String currencyCode;
    @NotNull  protected BigDecimal availableMoney;

    public static PortfolioDto fromPortfolio(Portfolio portfolio) {
        var dto = new PortfolioDto();
        dto.setName(portfolio.getName());
        dto.setCurrencyCode(portfolio.getCurrency().getCode());
        dto.setAvailableMoney(portfolio.getAvailableMoney());

        return dto;
    }

    @Override
    public String toString() {
        return "Portfolio: " +
                "name='" + name + '\'' +
                ", availableMoney=" + availableMoney + " " + currencyCode;
    }
}
