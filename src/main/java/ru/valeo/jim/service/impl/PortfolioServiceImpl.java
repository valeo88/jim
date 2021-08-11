package ru.valeo.jim.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.*;
import ru.valeo.jim.dto.InstrumentPositionDto;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.dto.PortfolioInstrumentsDistributionDto;
import ru.valeo.jim.dto.operation.*;
import ru.valeo.jim.exception.CurrencyNotFoundException;
import ru.valeo.jim.exception.InstrumentCategoryNotFoundException;
import ru.valeo.jim.exception.PortfolioNotFoundException;
import ru.valeo.jim.exception.UnexpectedValueException;
import ru.valeo.jim.repository.*;
import ru.valeo.jim.service.PortfolioService;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final OperationRepository operationRepository;
    private final CurrencyRepository currencyRepository;
    private final InstrumentPriceRepository instrumentPriceRepository;
    private final InstrumentCategoryRepository instrumentCategoryRepository;
    private final ApplicationConfig applicationConfig;

    @Transactional(readOnly = true)
    @Override
    public List<PortfolioDto> getPortfolios() {
        return portfolioRepository.findAll().stream().map(PortfolioDto::fromPortfolio).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PortfolioDto> getPortfolio(@NotBlank String name) {
        return portfolioRepository.findById(name).map(PortfolioDto::fromPortfolio);
    }

    @Transactional
    @Override
    public PortfolioDto save(@NotNull PortfolioDto dto) {
        var currency = currencyRepository.findById(dto.getCurrencyCode())
                .orElseThrow(() -> new CurrencyNotFoundException(dto.getCurrencyCode()));

        var portfolio = portfolioRepository.findById(dto.getName()).orElseGet(Portfolio::new);
        portfolio.setName(dto.getName());
        portfolio.setCurrency(currency);
        // don't set available money on save!
        if (StringUtils.hasText(dto.getCategoriesTargetDistribution())) {
            portfolio.setCategoryTargetDistributions(
                    parseInstrumentCategoryDistribution(dto.getCategoriesTargetDistribution())
                            .entrySet().stream().map(entry -> {
                        var e = new InstrumentCategoryTargetDistribution();
                        e.setPortfolio(portfolio);
                        e.setCategory(entry.getKey());
                        e.setPercent(entry.getValue());
                        return e;
                    }).collect(Collectors.toList())
            );
        }


        return PortfolioDto.fromPortfolio(portfolioRepository.save(portfolio));
    }

    @Transactional
    @Override
    public boolean delete(@NotBlank String name) {
        var portfolioOpt = portfolioRepository.findById(name);
        if (portfolioOpt.isPresent()) {
            portfolioRepository.delete(portfolioOpt.get());
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    @Override
    public void setDefault(@NotBlank String name) {
        var portfolioOpt = portfolioRepository.findById(name);
        if (portfolioOpt.isPresent()) {
            applicationConfig.setDefaultPortfolioName(name);
        } else {
            throw new PortfolioNotFoundException(name);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PortfolioDto> getDefault() {
        return ofNullable(applicationConfig.getDefaultPortfolioName())
                .flatMap(portfolioRepository::findById)
                .map(PortfolioDto::fromPortfolio);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InstrumentPositionDto> getInstrumentPositions(@Nullable String portfolioName) {
        return portfolioRepository.findById(getOrDefaultPortfolioName(portfolioName))
                .map(Portfolio::getPositions)
                .map(positions -> positions.stream()
                        .map(InstrumentPositionDto::from).collect(Collectors.toList()))
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
    }

    @Transactional(readOnly = true)
    @Override
    public List<OperationDto> getProcessedOperations(@Nullable String portfolioName) {
        return portfolioRepository.findById(getOrDefaultPortfolioName(portfolioName))
                .map(Portfolio::getOperations)
                .map(operations -> operations.stream()
                        .sorted(Comparator.comparing(Operation::getWhenAdd, Comparator.reverseOrder()))
                        .map(PortfolioServiceImpl::mapOperation)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
    }

    @Transactional(readOnly = true)
    @Override
    public PortfolioInstrumentsDistributionDto getInstrumentsDistributionByAccoutingPrice(String portfolioName) {
        return portfolioRepository.findById(getOrDefaultPortfolioName(portfolioName))
                .map(Portfolio::getPositions)
                .map(positions -> PortfolioInstrumentsDistributionDto.byAccountingPrice(positions,
                        applicationConfig.getBigdecimalOperationsScale()))
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
    }

    @Transactional(readOnly = true)
    @Override
    public PortfolioInstrumentsDistributionDto getInstrumentsDistributionByActualPrice(String portfolioName, LocalDateTime date) {
        Map<Instrument, BigDecimal> actualPrices = instrumentPriceRepository
                .findByWhenAddLessThanEqualOrderByWhenAddDesc(ofNullable(date).orElseGet(LocalDateTime::now))
                .stream()
                .collect(Collectors.toMap(InstrumentPrice::getInstrument, InstrumentPrice::getPrice, (a, b) -> a));
        return portfolioRepository.findById(getOrDefaultPortfolioName(portfolioName))
                .map(Portfolio::getPositions)
                .map(positions -> PortfolioInstrumentsDistributionDto.byActualPrice(positions,
                        actualPrices, applicationConfig.getBigdecimalOperationsScale()))
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
    }

    @Transactional(readOnly = true)
    @Override
    public PortfolioInstrumentsDistributionDto getTargetInstrumentsDistribution(String portfolioName) {
        return portfolioRepository.findById(getOrDefaultPortfolioName(portfolioName))
                .map(PortfolioInstrumentsDistributionDto::byTargetPercent)
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
    }

    @Transactional
    @Override
    public void reinit(@NotBlank String portfolioName) {
        var portfolio = portfolioRepository.findById(portfolioName)
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
        operationRepository.deleteAll(portfolio.getOperations());
        portfolio.getPositions().clear();
        portfolio.setAvailableMoney(BigDecimal.ZERO);
        portfolioRepository.save(portfolio);
    }

    // todo may be create mapper class
    private static OperationDto mapOperation(Operation operation) {
        switch (operation.getType()) {
            case ADD_MONEY:
                return AddMoneyDto.from(operation);
            case WITHDRAW_MONEY:
                return WithdrawMoneyDto.from(operation);
            case BUY:
                return BuyInstrumentDto.from(operation);
            case SELL:
                return SellInstrumentDto.from(operation);
            case COUPON:
                return CouponDto.from(operation);
            case DIVIDEND:
                return DividendDto.from(operation);
            case BOND_REDEMPTION:
                return BondRedemptionDto.from(operation);
            case TAX:
                return TaxDto.from(operation);
            default:
                throw new UnsupportedOperationException(operation.getType().name());
        }
    }

    private String getOrDefaultPortfolioName(@Nullable String name) {
        return ofNullable(ofNullable(name).orElseGet(applicationConfig::getDefaultPortfolioName))
                .orElseThrow(() -> new IllegalArgumentException("Portfolio name is null and default portfolio is not set!"));
    }

    private Map<InstrumentCategory, BigDecimal> parseInstrumentCategoryDistribution(String value) {
        var categoryCodeDistr = ofNullable(value)
                .filter(StringUtils::hasText)
                .map(d -> d.split(","))
                .map(t -> Arrays.stream(t).map(m -> m.split("-"))
                        .collect(Collectors.toMap(x -> instrumentCategoryRepository.findById(x[0])
                                        .orElseThrow(() -> new InstrumentCategoryNotFoundException(x[0])),
                                x -> new BigDecimal(x[1]))))
                .orElseGet(Collections::emptyMap);
        var categoriesSum = categoryCodeDistr.values().stream()
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        if (categoriesSum.compareTo(BigDecimal.ZERO) > 0 && !categoriesSum.equals(new BigDecimal(100))) {
            throw new UnexpectedValueException(new BigDecimal(100), categoriesSum);
        }
        return categoryCodeDistr;
    }
}
