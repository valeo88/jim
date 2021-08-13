package ru.valeo.jim.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.*;
import ru.valeo.jim.dto.PortfolioRebalancePropositionDto;
import ru.valeo.jim.repository.InstrumentPriceRepository;
import ru.valeo.jim.service.RebalanceService;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Service
public class RebalanceServiceImpl implements RebalanceService {

    private final InstrumentPriceRepository instrumentPriceRepository;
    private final ApplicationConfig applicationConfig;

    @Transactional(readOnly = true)
    @Override
    public PortfolioRebalancePropositionDto rebalance(@NotNull Portfolio portfolio) {
        var result = new PortfolioRebalancePropositionDto();
        result.setPortfolioName(portfolio.getName());

        var targetPercentDistribution = portfolio.getCategoryTargetDistributions()
                .stream()
                .collect(Collectors.toMap(InstrumentCategoryTargetDistribution::getCategory,
                        InstrumentCategoryTargetDistribution::getPercent));
        if (targetPercentDistribution.isEmpty()) {
            return result;
        }

        final var totalPriceByCategoryActual = getTotalPricesByCategory(portfolio.getPositions());
        final var totalInstrumentsActualPrice = totalPriceByCategoryActual.values().stream()
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Map<InstrumentCategory, BigDecimal> totalPriceByCategoryTarget = targetPercentDistribution.entrySet()
                .stream().map(entry -> {
                    entry.setValue(entry.getValue().multiply(totalInstrumentsActualPrice)
                            .divide(new BigDecimal(100),
                                    applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR));
                    return entry;
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
