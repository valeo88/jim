package ru.valeo.jim.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class PortfolioInstrumentsDistributionDto {

    @NotBlank
    private String portfolioName;
    private Map<InstrumentCategory, BigDecimal> percentByCategory = new HashMap<>();

    public static PortfolioInstrumentsDistributionDto byAccountingPrice(@NotNull List<InstrumentPosition> instrumentPositions,
                                                                        int bigdecimalOperationsScale) {
        return byActualPrice(instrumentPositions, Collections.emptyMap(), bigdecimalOperationsScale);
    }

    public static PortfolioInstrumentsDistributionDto byActualPrice(@NotNull List<InstrumentPosition> instrumentPositions,
                                                                    @NotNull Map<Instrument, BigDecimal> actualPrices,
                                                                    int bigdecimalOperationsScale) {
        var dto = new PortfolioInstrumentsDistributionDto();
        var sum = BigDecimal.ZERO;
        for (var position: instrumentPositions) {
            var instrumentPrice = ofNullable(actualPrices.get(position.getInstrument()))
                    .orElse(position.getAccountingPrice());
            var positionPrice = instrumentPrice.multiply(BigDecimal.valueOf(position.getAmount()));
            sum = sum.add(positionPrice);
            dto.getPercentByCategory().compute(position.getInstrument().getCategory(), (k,v) -> {
                if (isNull(v)) {
                    return positionPrice;
                } else {
                    return v.add(positionPrice);
                }
            });
            if (isNull(dto.getPortfolioName())) {
                dto.setPortfolioName(position.getPortfolio().getName());
            }
        }
        // calc percents
        for (var entry : dto.getPercentByCategory().entrySet()) {
            entry.setValue(entry.getValue().divide(sum, bigdecimalOperationsScale, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100L)));
        }
        return dto;
    }

    public static PortfolioInstrumentsDistributionDto byTargetPercent(@NotNull Portfolio portfolio) {
        var dto = new PortfolioInstrumentsDistributionDto();
        dto.setPortfolioName(portfolio.getName());
        dto.setPercentByCategory(ofNullable(portfolio.getCategoryTargetDistributions())
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(InstrumentCategoryTargetDistribution::getCategory,
                        InstrumentCategoryTargetDistribution::getPercent)));
        return dto;
    }

    @Override
    public String toString() {
        return getPercentByCategory().entrySet().stream()
                .map(entry -> entry.getKey().getName() + ": " + entry.getValue() + "%")
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
