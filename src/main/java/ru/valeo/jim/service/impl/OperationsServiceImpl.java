package ru.valeo.jim.service.impl;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.*;
import ru.valeo.jim.dto.operation.AddMoneyDto;
import ru.valeo.jim.dto.operation.BuyInstrumentDto;
import ru.valeo.jim.dto.operation.WithdrawMoneyDto;
import ru.valeo.jim.exception.InstrumentNotFoundException;
import ru.valeo.jim.exception.InsufficientMoneyException;
import ru.valeo.jim.exception.PortfolioNotFoundException;
import ru.valeo.jim.repository.InstrumentRepository;
import ru.valeo.jim.repository.OperationRepository;
import ru.valeo.jim.repository.PortfolioRepository;
import ru.valeo.jim.service.OperationsService;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Service
public class OperationsServiceImpl implements OperationsService {

    private final ApplicationConfig applicationConfig;
    private final OperationRepository operationRepository;
    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;

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
        checkIsMoneySufficient(portfolio, dto.getValue());

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

    @Transactional
    @Override
    public BuyInstrumentDto buyInstrument(@NotNull BuyInstrumentDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        checkIsMoneySufficient(portfolio, dto.getTotalPrice());

        var operation = new Operation();
        operation.setType(OperationType.BUY);
        operation.setInstrument(instrument);
        operation.setPortfolio(portfolio);
        operation.setPrice(dto.getPrice());
        operation.setAmount(dto.getAmount());
        operation.setWhenAdd(LocalDateTime.now());
        operation = operationRepository.save(operation);

        processOperation(operation);
        return BuyInstrumentDto.from(operation);
    }

    private Instrument loadInstrument(String symbol) {
        return instrumentRepository.findById(symbol)
                .orElseThrow(() -> new InstrumentNotFoundException(symbol));
    }

    private Portfolio loadPortfolio(String name) {
        return portfolioRepository.findById(ofNullable(name)
                .orElse(applicationConfig.getDefaultPortfolioName()))
                .orElseThrow(() -> new PortfolioNotFoundException(name));
    }

    private void checkIsMoneySufficient(Portfolio portfolio, BigDecimal value) {
        if (portfolio.getAvailableMoney().compareTo(value) < 0)
            throw new InsufficientMoneyException(portfolio.getName());
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
            case BUY:
                portfolio.setAvailableMoney(portfolio.getAvailableMoney().subtract(operation.getTotalPrice()));
                updateInstrumentPositionOnBuy(operation);
                break;
            default:
                throw new UnsupportedOperationException(operation.getType().name());
        }
        operationRepository.save(operation);
        portfolioRepository.save(portfolio);
    }

    private void updateInstrumentPositionOnBuy(Operation operation) {
        var portfolio = operation.getPortfolio();
        var currentPosition = portfolio.getPositions().stream()
                .filter(instrumentPosition -> instrumentPosition.getInstrument().equals(operation.getInstrument()))
                .findFirst();
        if (currentPosition.isPresent()) {
            // update existing: need to recalc accounting price
            var position = currentPosition.get();
            position.setAmount(position.getAmount() + operation.getAmount());
            // calc new accounting price
            Map<BigDecimal, Integer> priceAmountMap = new HashMap<>();
            priceAmountMap.put(operation.getPrice(), operation.getAmount());
            portfolio.getOperations().stream()
                    .filter(Operation::getProcessed)
                    .filter(op -> !op.getDeleted())
                    .filter(op -> op.getType() == OperationType.BUY)
                    .filter(op -> op.getInstrument().equals(operation.getInstrument()))
                    .forEach(op -> {
                        priceAmountMap.compute(op.getPrice(),
                                (price, amount) -> amount == null ? op.getAmount() : amount + op.getAmount());
                    });
            var priceAmountMultiplicationSum = priceAmountMap.entrySet().stream()
                    .map(entry -> entry.getKey().multiply(BigDecimal.valueOf(entry.getValue())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            var count = BigDecimal.valueOf(priceAmountMap.values().stream().reduce(0, Integer::sum));
            position.setAccountingPrice(priceAmountMultiplicationSum.divide(count));
        } else {
            // add new position: operation price === accounting price
            var newPosition = new InstrumentPosition();
            newPosition.setPortfolio(portfolio);
            newPosition.setInstrument(operation.getInstrument());
            newPosition.setAmount(operation.getAmount());
            newPosition.setAccountingPrice(operation.getPrice());
            portfolio.getPositions().add(newPosition);
        }
    }
}
