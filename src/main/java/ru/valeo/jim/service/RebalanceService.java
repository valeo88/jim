package ru.valeo.jim.service;

import ru.valeo.jim.domain.Portfolio;
import ru.valeo.jim.dto.PortfolioRebalancePropositionDto;

import javax.validation.constraints.NotNull;

/** Portfolio rebalancing propositions. */
public interface RebalanceService {

    /** Get rebalance proposition by target portfolio categories distribution. */
    PortfolioRebalancePropositionDto rebalance(@NotNull Portfolio portfolio);
}
