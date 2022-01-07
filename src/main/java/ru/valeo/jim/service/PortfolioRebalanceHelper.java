package ru.valeo.jim.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.InstrumentCategory;
import ru.valeo.jim.domain.InstrumentCategoryTargetDistribution;
import ru.valeo.jim.domain.InstrumentPosition;
import ru.valeo.jim.domain.InstrumentPrice;
import ru.valeo.jim.domain.OperationType;
import ru.valeo.jim.domain.Portfolio;
import ru.valeo.jim.dto.PortfolioRebalancePropositionDto;
import ru.valeo.jim.repository.InstrumentPriceRepository;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Service
public class PortfolioRebalanceHelper {

    private final InstrumentPriceRepository instrumentPriceRepository;
    private final ApplicationConfig applicationConfig;

    @Transactional(readOnly = true)
    public PortfolioRebalancePropositionDto rebalance(@NotNull Portfolio portfolio, boolean useAvailableMoney) {
        var result = new PortfolioRebalancePropositionDto();
        result.setPortfolioName(portfolio.getName());

        var targetPercentDistribution = portfolio.getCategoryTargetDistributions()
                .stream()
                .collect(Collectors.toMap(InstrumentCategoryTargetDistribution::getCategory,
                        InstrumentCategoryTargetDistribution::getPercent));
        if (targetPercentDistribution.isEmpty()) {
            return result;
        }

        final var availableMoney = useAvailableMoney ? portfolio.getAvailableMoney() : BigDecimal.ZERO;
        final var instrumentPositions = portfolio.getPositions().stream()
                .filter(position -> !position.getExcludeFromDistribution())
                .collect(Collectors.toList());
        final var totalPriceByCategoryActual = getTotalPricesByCategory(instrumentPositions);
        final var totalInstrumentsActualPrice = totalPriceByCategoryActual.values().stream()
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Map<InstrumentCategory, BigDecimal> totalPriceByCategoryTarget = targetPercentDistribution.entrySet()
                .stream().peek(entry -> {
                    var targetByActualPrice = entry.getValue().multiply(totalInstrumentsActualPrice)
                            .divide(new BigDecimal(100), applicationConfig.getBigdecimalOperationsScale(),
                                    RoundingMode.FLOOR);
                    var targetByAvailableMoney = entry.getValue().multiply(availableMoney)
                            .divide(new BigDecimal(100), applicationConfig.getBigdecimalOperationsScale(),
                                    RoundingMode.FLOOR);
                    entry.setValue(targetByActualPrice.add(targetByAvailableMoney));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // process all actual distribution and fill operations
        totalPriceByCategoryActual.forEach((instrumentCategory, totalPrice) -> {
            BigDecimal diff = totalPriceByCategoryTarget.getOrDefault(instrumentCategory, BigDecimal.ZERO)
                    .subtract(totalPrice);
            if (!diff.equals(BigDecimal.ZERO)) {
                result.getOperations().add(new PortfolioRebalancePropositionDto.RebalanceOperation()
                        .setCategoryCode(instrumentCategory.getCode())
                        .setCategoryName(instrumentCategory.getName())
                        .setOperation(diff.compareTo(BigDecimal.ZERO) > 0 ? OperationType.BUY.name() : OperationType.SELL.name())
                        .setSum(diff.abs())
                );
            }
        });
        // process all categories, that have no actual instruments positions
        totalPriceByCategoryTarget.entrySet().stream()
                .filter(entry -> !totalPriceByCategoryActual.containsKey(entry.getKey()))
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .forEach(entry -> result.getOperations().add(new PortfolioRebalancePropositionDto.RebalanceOperation()
                        .setCategoryCode(entry.getKey().getCode())
                        .setCategoryName(entry.getKey().getName())
                        .setOperation(OperationType.BUY.name())
                        .setSum(entry.getValue())
                ));

        return result;
    }

    /** Get map by actual instrument prices with:
     * key - instrument category
     * value - sum of total actual prices of instruments in category. */
    private Map<InstrumentCategory, BigDecimal> getTotalPricesByCategory(@NotNull List<InstrumentPosition> positions) {
        var actualPrices = instrumentPriceRepository
                .findByWhenAddLessThanEqualOrderByWhenAddDesc(LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(InstrumentPrice::getInstrument, InstrumentPrice::getPrice, (a, b) -> a));
        Map<InstrumentCategory, BigDecimal> data = new HashMap<>();
        for (var position: positions) {
            var instrumentPrice = ofNullable(actualPrices.get(position.getInstrument()))
                    .orElse(position.getAccountingPrice());
            var positionPrice = instrumentPrice.multiply(BigDecimal.valueOf(position.getAmount()));
            data.compute(position.getInstrument().getCategory(), (k,v) -> {
                if (isNull(v)) {
                    return positionPrice;
                } else {
                    return v.add(positionPrice);
                }
            });
        }
        return data;
    }
}
