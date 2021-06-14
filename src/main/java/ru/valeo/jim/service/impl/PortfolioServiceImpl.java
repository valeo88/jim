package ru.valeo.jim.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.Operation;
import ru.valeo.jim.domain.Portfolio;
import ru.valeo.jim.dto.InstrumentPositionDto;
import ru.valeo.jim.dto.PortfolioDto;
import ru.valeo.jim.dto.operation.*;
import ru.valeo.jim.exception.CurrencyNotFoundException;
import ru.valeo.jim.exception.PortfolioNotFoundException;
import ru.valeo.jim.repository.CurrencyRepository;
import ru.valeo.jim.repository.PortfolioRepository;
import ru.valeo.jim.service.PortfolioService;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final CurrencyRepository currencyRepository;
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
    public List<InstrumentPositionDto> getInstrumentPositions(String portfolioName) {
        return portfolioRepository.findById(ofNullable(portfolioName)
                .orElse(applicationConfig.getDefaultPortfolioName()))
                .map(Portfolio::getPositions)
                .map(positions -> positions.stream()
                        .map(InstrumentPositionDto::from).collect(Collectors.toList()))
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
    }

    @Transactional(readOnly = true)
    @Override
    public List<OperationDto> getProcessedOperations(String portfolioName) {
        return portfolioRepository.findById(ofNullable(portfolioName)
                .orElse(applicationConfig.getDefaultPortfolioName()))
                .map(Portfolio::getOperations)
                .map(operations -> operations.stream()
                        .sorted(Comparator.comparing(Operation::getWhenAdd, Comparator.reverseOrder()))
                        .map(PortfolioServiceImpl::mapOperation)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioName));
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
}
