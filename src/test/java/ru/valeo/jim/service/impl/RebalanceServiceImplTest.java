package ru.valeo.jim.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.*;
import ru.valeo.jim.repository.InstrumentPriceRepository;
import ru.valeo.jim.service.RebalanceService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RebalanceServiceImplTest {

    private static final List<InstrumentCategory> categories;
    private static final List<Instrument> instruments;
    private static final List<InstrumentPrice> actualPrices;

    static {
        var cat1 = new InstrumentCategory();
        cat1.setCode("1");
        var cat2 = new InstrumentCategory();
        cat2.setCode("2");

        categories = List.of(cat1, cat2);

        var instr1 = new Instrument();
        instr1.setSymbol("1");
        instr1.setCategory(cat1);
        var instr2 = new Instrument();
        instr2.setSymbol("2");
        instr2.setCategory(cat2);

        instruments = List.of(instr1, instr2);

        var price1 = new InstrumentPrice();
        price1.setInstrument(instr1);
        price1.setPrice(new BigDecimal(50));
        var price2 = new InstrumentPrice();
        price2.setInstrument(instr2);
        price2.setPrice(new BigDecimal(100));

        actualPrices = List.of(price1, price2);
    }

    @Mock
    private InstrumentPriceRepository instrumentPriceRepository;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private RebalanceService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RebalanceServiceImpl(instrumentPriceRepository, applicationConfig);
    }

    @Test
    public void shouldRebalanceOnTwoInstruments() {
        when(instrumentPriceRepository.findByWhenAddLessThanEqualOrderByWhenAddDesc(any(LocalDateTime.class)))
                .thenReturn(actualPrices);
        var pos1 = new InstrumentPosition();
        pos1.setInstrument(instruments.get(0));
        pos1.setAmount(5);
        pos1.setAccountingPrice(new BigDecimal(40));
        var pos2 = new InstrumentPosition();
        pos2.setInstrument(instruments.get(1));
        pos2.setAmount(7);
        pos2.setAccountingPrice(new BigDecimal(95));

        var distr1 = new InstrumentCategoryTargetDistribution();
        distr1.setCategory(categories.get(0));
        distr1.setPercent(new BigDecimal(30));
        var distr2 = new InstrumentCategoryTargetDistribution();
        distr2.setCategory(categories.get(1));
        distr2.setPercent(new BigDecimal(70));

        var portfolio = new Portfolio();
        portfolio.setName("Alpha");
        portfolio.setAvailableMoney(BigDecimal.ZERO);
        portfolio.setPositions(List.of(pos1, pos2));
        portfolio.setCategoryTargetDistributions(List.of(distr1, distr2));

        var result = service.rebalance(portfolio);

        assertEquals(portfolio.getName(), result.getPortfolioName());
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

}