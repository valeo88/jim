package ru.valeo.jim.service;

import ru.valeo.jim.dto.PortfolioDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/** Service for financial portfolios */
public interface PortfolioService {

    /** Get all available portfolios. */
    List<PortfolioDto> getPortfolios();

    /** Save portfolio from DTO. */
    PortfolioDto save(@NotNull PortfolioDto dto);

    /** Delete portfolio by it's name. */
    boolean delete(@NotBlank String name);

    /** Set default portfolio. */
    void setDefault(@NotBlank String name);
}