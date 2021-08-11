package ru.valeo.jim.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.Portfolio;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class PortfolioDto {

    @NotBlank protected String name;
    @NotBlank protected String currencyCode;
    @NotNull  protected BigDecimal availableMoney;
    protected String categoriesTargetDistribution;

    public static PortfolioDto fromPortfolio(Portfolio portfolio) {
        var dto = new PortfolioDto();
        dto.setName(portfolio.getName());
        dto.setCurrencyCode(portfolio.getCurrency().getCode());
        dto.setAvailableMoney(portfolio.getAvailableMoney());
        dto.setCategoriesTargetDistribution(
            ofNullable(portfolio.getCategoryTargetDistributions())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(d -> d.getCategory().getCode() + "-" + d.getPercent() + "%")
            .collect(Collectors.joining(","))
        );

        return dto;
    }

    @Override
    public String toString() {
        return "Portfolio: " +
                "name='" + name + '\'' +
                ", availableMoney=" + availableMoney + " " + currencyCode +
                ", categories target distribution: " + categoriesTargetDistribution;
    }
}
