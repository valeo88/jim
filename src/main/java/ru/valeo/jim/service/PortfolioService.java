package ru.valeo.jim.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;
import ru.valeo.jim.dto.InstrumentPositionDto;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.dto.PortfolioInstrumentsDistributionDto;
import ru.valeo.jim.dto.PortfolioRebalancePropositionDto;
import ru.valeo.jim.dto.operation.OperationDto;

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
    List<InstrumentPositionDto> getInstrumentPositions(@Nullable String portfolioName);

    /** Get all processed operations in portfolio. */
    List<OperationDto> getProcessedOperations(@Nullable String portfolioName);

    /** Get instruments distribution in portfolio by accounting prices */
    PortfolioInstrumentsDistributionDto getInstrumentsDistributionByAccoutingPrice(@Nullable String portfolioName);

    /** Get instruments distribution in portfolio by actual prices
     * @param date - date for what actual prices will search*/
    PortfolioInstrumentsDistributionDto getInstrumentsDistributionByActualPrice(@Nullable String portfolioName,
                                                                                @Nullable LocalDateTime date);

    /** Get target instruments distribution in portfolio. */
    PortfolioInstrumentsDistributionDto getTargetInstrumentsDistribution(@Nullable String portfolioName);

    /** Get rebalance proposition by target portfolio categories distribution.
     * @param useAvailableMoney - use all available money in portfolio. */
    PortfolioRebalancePropositionDto getRebalanceProposition(@Nullable String portfolioName, boolean useAvailableMoney);

    /** Reinitialize portfolio:
     * - delete all operations
     * - delete all instrument positions
     * - set available money to 0 */
    void reinit(@NotBlank String portfolioName);

    /** Switching setting excludeFromDistribution on InstrumentPosition in portfolio*/
    void toggleExcludeInstrumentFromDistribution(@NotBlank String symbol, String portfolioName);
}
