package ru.valeo.jim.service.impl;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.Operation;
import ru.valeo.jim.domain.OperationType;
import ru.valeo.jim.domain.Portfolio;
import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;
import ru.valeo.jim.exception.InsufficientMoneyException;
import ru.valeo.jim.exception.PortfolioNotFoundException;
import ru.valeo.jim.repository.OperationRepository;
import ru.valeo.jim.repository.PortfolioRepository;
import ru.valeo.jim.service.OperationsService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Service
public class OperationsServiceImpl implements OperationsService {

    private final ApplicationConfig applicationConfig;
    private final OperationRepository operationRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    @Override
    public AddMoneyDto addMoney(@NotNull AddMoneyDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var operation = new Operation();
        operation.setAmount(1);
        operation.setPortfolio(portfolio);
        operation.setPrice(dto.getValue());
        operation.setType(OperationType.ADD_MONEY);
        operation.setWhenAdd(LocalDateTime.now());
        operation = operationRepository.save(operation);

        processOperation(operation);
        return AddMoneyDto.from(operation);
    }

    @Transactional
    @Override
    public WithdrawMoneyDto withdrawMoney(@NotNull WithdrawMoneyDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        if (portfolio.getAvailableMoney().compareTo(dto.getValue()) < 0)
            throw new InsufficientMoneyException(portfolio.getName());
        var operation = new Operation();
        operation.setAmount(1);
        operation.setPortfolio(portfolio);
        operation.setPrice(dto.getValue());
        operation.setType(OperationType.WITHDRAW_MONEY);
        operation.setWhenAdd(LocalDateTime.now());
        operation = operationRepository.save(operation);

        processOperation(operation);
        return WithdrawMoneyDto.from(operation);
    }

    private Portfolio loadPortfolio(String name) {
        return portfolioRepository.findById(ofNullable(name)
                .orElse(applicationConfig.getDefaultPortfolioName()))
                .orElseThrow(() -> new PortfolioNotFoundException(name));
    }

    /** Process operation. */
    private void processOperation(Operation operation) {
        operation.setProcessed(true);
        var portfolio = operation.getPortfolio();
        switch (operation.getType()) {
            case ADD_MONEY:
                portfolio.setAvailableMoney(portfolio.getAvailableMoney().add(operation.getTotalPrice()));
                break;
            case WITHDRAW_MONEY:
                portfolio.setAvailableMoney(portfolio.getAvailableMoney().subtract(operation.getTotalPrice()));
                break;
            default:
                throw new UnsupportedOperationException(operation.getType().name());
        }
        operationRepository.save(operation);
        portfolioRepository.save(portfolio);
    }
}
