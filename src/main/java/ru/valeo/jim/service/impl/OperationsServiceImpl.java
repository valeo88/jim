package ru.valeo.jim.service.impl;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.Operation;
import ru.valeo.jim.domain.OperationType;
import ru.valeo.jim.domain.Portfolio;
import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.exception.PortfolioNotFoundException;
import ru.valeo.jim.repository.OperationRepository;
import ru.valeo.jim.repository.PortfolioRepository;
import ru.valeo.jim.service.OperationsService;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Service
public class OperationsServiceImpl implements OperationsService {

    private final ApplicationConfig applicationConfig;
    private final OperationRepository operationRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    @Override
    public AddMoneyDto addMoney(String portfolioName, @NotNull @Min(0) BigDecimal value) {
        var portfolio = loadPortfolio(portfolioName);
        var operation = new Operation();
        operation.setAmount(1);
        operation.setPortfolio(portfolio);
        operation.setPrice(value);
        operation.setType(OperationType.ADD_MONEY);
        return AddMoneyDto.from(operationRepository.save(operation));
    }

    @Transactional
    @Override
    public void withdrawMoney(String portfolioName, @NotNull @Min(0) BigDecimal value) {
        // todo implement
    }

    private Portfolio loadPortfolio(String name) {
        return portfolioRepository.findById(ofNullable(name)
                .orElse(applicationConfig.getDefaultPortfolioName()))
                .orElseThrow(() -> new PortfolioNotFoundException(name));
    }
}
