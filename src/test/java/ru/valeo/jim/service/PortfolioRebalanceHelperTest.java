package ru.valeo.jim.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.*;
import ru.valeo.jim.repository.InstrumentPriceRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PortfolioRebalanceHelperTest {

    private static final List<InstrumentCategory> categories;
    private static final List<Instrument> instruments;
    private static final List<InstrumentPrice> actualPrices;

    static {
        var cat1 = new InstrumentCategory().setCode("1");
        var cat2 = new InstrumentCategory().setCode("2");
        var cat3 = new InstrumentCategory().setCode("3");

        categories = List.of(cat1, cat2, cat3);

        var instr1 = new Instrument().setSymbol("1").setCategory(cat1);
        var instr2 = new Instrument().setSymbol("2").setCategory(cat2);
        var instr3 = new Instrument().setSymbol("3").setCategory(cat3);

        instruments = List.of(instr1, instr2, instr3);

        var price1 = new InstrumentPrice().setInstrument(instr1).setPrice(new BigDecimal(50));
        var price2 = new InstrumentPrice().setInstrument(instr2).setPrice(new BigDecimal(100));
        var price3 = new InstrumentPrice().setInstrument(instr3).setPrice(new BigDecimal(30));

        actualPrices = List.of(price1, price2, price3);
    }

    @Mock
    private InstrumentPriceRepository instrumentPriceRepository;
    private PortfolioRebalanceHelper helper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        helper = new PortfolioRebalanceHelper(instrumentPriceRepository, new ApplicationConfig());
        when(instrumentPriceRepository.findByWhenAddLessThanEqualOrderByWhenAddDesc(any(LocalDateTime.class)))
                .thenReturn(actualPrices);
    }

    @Test
    void shouldRebalanceOnTwoCategories() {
        var pos1 = new InstrumentPosition().setInstrument(instruments.get(0)).setAmount(5)
                .setAccountingPrice(new BigDecimal(40));
        var pos2 = new InstrumentPosition().setInstrument(instruments.get(1)).setAmount(7)
                .setAccountingPrice(new BigDecimal(95));

        var distr1 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(0))
                .setPercent(new BigDecimal(30));
        var distr2 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(1))
                .setPercent(new BigDecimal(70));

        var portfolio = new Portfolio().setName("Alpha").setAvailableMoney(BigDecimal.ZERO)
            .setPositions(List.of(pos1, pos2))
            .setCategoryTargetDistributions(List.of(distr1, distr2));

        var result = helper.rebalance(portfolio, false);

        assertEquals(portfolio.getName(), result.getPortfolioName());
        assertEquals(2, result.getOperations().size());
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("1")
                        && op.getOperation().equals(OperationType.BUY.name())
                        && op.getSum().equals(new BigDecimal("35.000"))
                ));
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("2")
                        && op.getOperation().equals(OperationType.SELL.name())
                        && op.getSum().equals(new BigDecimal("35.000"))
                ));

    }

    @Test
    void shouldRebalanceOnThreeCategoriesWithoutAvailableMoney() {
        var pos1 = new InstrumentPosition().setInstrument(instruments.get(0)).setAmount(5)
                .setAccountingPrice(new BigDecimal(40));
        var pos2 = new InstrumentPosition().setInstrument(instruments.get(1)).setAmount(7)
                .setAccountingPrice(new BigDecimal(95));
        var pos3 = new InstrumentPosition().setInstrument(instruments.get(2)).setAmount(4)
                .setAccountingPrice(new BigDecimal(33));

        var distr1 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(0))
                .setPercent(new BigDecimal(20));
        var distr2 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(1))
                .setPercent(new BigDecimal(70));
        var distr3 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(2))
                .setPercent(new BigDecimal(10));

        var portfolio = new Portfolio().setName("Alpha").setAvailableMoney(BigDecimal.ZERO)
                .setPositions(List.of(pos1, pos2, pos3))
                .setCategoryTargetDistributions(List.of(distr1, distr2, distr3));

        var result = helper.rebalance(portfolio, false);

        assertEquals(portfolio.getName(), result.getPortfolioName());
        assertEquals(3, result.getOperations().size());
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("1")
                        && op.getOperation().equals(OperationType.SELL.name())
                        && op.getSum().equals(new BigDecimal("36.000"))
                ));
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("2")
                        && op.getOperation().equals(OperationType.BUY.name())
                        && op.getSum().equals(new BigDecimal("49.000"))
                ));
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("3")
                        && op.getOperation().equals(OperationType.SELL.name())
                        && op.getSum().equals(new BigDecimal("13.000"))
                ));

    }

    @Test
    void shouldRebalanceOnThreeCategoriesWithAvailableMoney() {
        var pos1 = new InstrumentPosition().setInstrument(instruments.get(0)).setAmount(5)
                .setAccountingPrice(new BigDecimal(40));
        var pos2 = new InstrumentPosition().setInstrument(instruments.get(1)).setAmount(7)
                .setAccountingPrice(new BigDecimal(95));
        var pos3 = new InstrumentPosition().setInstrument(instruments.get(2)).setAmount(4)
                .setAccountingPrice(new BigDecimal(33));

        var distr1 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(0))
                .setPercent(new BigDecimal(20));
        var distr2 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(1))
                .setPercent(new BigDecimal(70));
        var distr3 = new InstrumentCategoryTargetDistribution().setCategory(categories.get(2))
                .setPercent(new BigDecimal(10));

        var portfolio = new Portfolio().setName("Alpha")
                .setAvailableMoney(BigDecimal.valueOf(200))
                .setPositions(List.of(pos1, pos2, pos3))
                .setCategoryTargetDistributions(List.of(distr1, distr2, distr3));

        var result = helper.rebalance(portfolio, true);

        assertEquals(portfolio.getName(), result.getPortfolioName());
        assertEquals(3, result.getOperations().size());
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("1")
                        && op.getOperation().equals(OperationType.BUY.name())
                        && op.getSum().equals(new BigDecimal("4.000"))
                ));
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("2")
                        && op.getOperation().equals(OperationType.BUY.name())
                        && op.getSum().equals(new BigDecimal("189.000"))
                ));
        assertTrue(result.getOperations().stream()
                .anyMatch(op -> op.getCategoryCode().equals("3")
                        && op.getOperation().equals(OperationType.BUY.name())
                        && op.getSum().equals(new BigDecimal("7.000"))
                ));

    }

}