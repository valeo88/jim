package ru.valeo.jim.service;

import ru.valeo.jim.dto.InstrumentPositionDto;
import ru.valeo.jim.dto.PortfolioDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/** Service for financial portfolios */
public interface PortfolioService {

    /** Get all available portfolios. */
    List<PortfolioDto> getPortfolios();

    /** Get portfolio by name. */
    Optional<PortfolioDto> getPortfolio(@NotBlank String name);

    /** Save portfolio from DTO. */
    PortfolioDto save(@NotNull PortfolioDto dto);

    /** Delete portfolio by it's name. */
    boolean delete(@NotBlank String name);

    /** Set default portfolio. */
    void setDefault(@NotBlank String name);

    /** Get default portfolio. */
    Optional<PortfolioDto> getDefault();

    /** Get all instrument positions in portfolio. */
    List<InstrumentPositionDto> getInstrumentPositions(@NotBlank String portfolioName);
}
