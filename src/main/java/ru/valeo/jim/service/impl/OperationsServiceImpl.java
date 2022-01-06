package ru.valeo.jim.service.impl;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeo.jim.config.ApplicationConfig;
import ru.valeo.jim.domain.*;
import ru.valeo.jim.dto.operation.*;
import ru.valeo.jim.exception.*;
import ru.valeo.jim.repository.InstrumentRepository;
import ru.valeo.jim.repository.OperationRepository;
import ru.valeo.jim.repository.PortfolioRepository;
import ru.valeo.jim.service.OperationsService;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
        var operation = new Operation()
            .setAmount(1)
            .setPortfolio(portfolio)
            .setPrice(dto.getValue())
            .setType(OperationType.ADD_MONEY)
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return AddMoneyDto.from(operation);
    }

    @Transactional
    @Override
    public WithdrawMoneyDto withdrawMoney(@NotNull WithdrawMoneyDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        checkIsMoneySufficient(portfolio, dto.getValue());

        var operation = new Operation()
            .setAmount(1)
            .setPortfolio(portfolio)
            .setPrice(dto.getValue())
            .setType(OperationType.WITHDRAW_MONEY)
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return WithdrawMoneyDto.from(operation);
    }

    @Transactional
    @Override
    public BuyInstrumentDto buyInstrument(@NotNull BuyInstrumentDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        validateInstrumentType(instrument.getType(), InstrumentType.typesWithoutCoupon());
        checkIsMoneySufficient(portfolio, dto.getTotalPrice());

        var operation = new Operation()
            .setType(OperationType.BUY)
            .setInstrument(instrument)
            .setPortfolio(portfolio)
            .setPrice(dto.getPrice())
            .setAccumulatedCouponIncome(BigDecimal.ZERO)
            .setAmount(dto.getAmount())
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return BuyInstrumentDto.from(operation);
    }

    @Transactional
    @Override
    public BuyBondDto buyBond(@NotNull BuyBondDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        validateInstrumentType(instrument.getType(), InstrumentType.typesWithCoupon());
        // accumulated coupon income + total price of bonds
        var totalPrice = dto.getAccumulatedCouponIncome().add(
                dto.getPercent().divide(BigDecimal.valueOf(100), applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR)
                        .multiply(instrument.getBondParValue())
                        .multiply(BigDecimal.valueOf(dto.getAmount()))
        );
        checkIsMoneySufficient(portfolio, totalPrice);

        var operation = new Operation()
            .setType(OperationType.BUY)
            .setInstrument(instrument)
            .setPortfolio(portfolio)
            .setPrice(instrument.getBondParValue().multiply(dto.getPercent())
                    .divide(BigDecimal.valueOf(100), applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR))
            .setAccumulatedCouponIncome(dto.getAccumulatedCouponIncome())
            .setPercent(dto.getPercent())
            .setAmount(dto.getAmount())
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return BuyBondDto.from(operation);
    }

    @Transactional
    @Override
    public SellInstrumentDto sellInstrument(@NotNull SellInstrumentDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        validateInstrumentType(instrument.getType(), InstrumentType.typesWithoutCoupon());
        checkIsAmountSufficient(portfolio, dto.getSymbol(), dto.getAmount());

        var operation = new Operation()
            .setType(OperationType.SELL)
            .setInstrument(instrument)
            .setPortfolio(portfolio)
            .setPrice(dto.getPrice())
            .setAccumulatedCouponIncome(BigDecimal.ZERO)
            .setAmount(dto.getAmount())
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return SellInstrumentDto.from(operation);
    }

    @Transactional
    @Override
    public SellBondDto sellBond(@NotNull SellBondDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        validateInstrumentType(instrument.getType(), InstrumentType.typesWithCoupon());
        checkIsAmountSufficient(portfolio, dto.getSymbol(), dto.getAmount());

        var operation = new Operation()
            .setType(OperationType.SELL)
            .setInstrument(instrument)
            .setPortfolio(portfolio)
            .setPrice(instrument.getBondParValue().multiply(dto.getPercent())
                    .divide(BigDecimal.valueOf(100), applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR))
            .setAccumulatedCouponIncome(dto.getAccumulatedCouponIncome())
            .setPercent(dto.getPercent())
            .setAmount(dto.getAmount())
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return SellBondDto.from(operation);
    }

    @Transactional
    @Override
    public DividendDto dividend(@NotNull DividendDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        validateInstrumentType(instrument.getType(), InstrumentType.typesWithDividend());
        checkIsAmountSufficient(portfolio, dto.getSymbol(), dto.getAmount());

        var operation = new Operation()
            .setType(OperationType.DIVIDEND)
            .setInstrument(instrument)
            .setPortfolio(portfolio)
            .setPrice(dto.getPrice())
            .setAmount(dto.getAmount())
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return DividendDto.from(operation);
    }

    @Transactional
    @Override
    public CouponDto coupon(@NotNull CouponDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        validateInstrumentType(instrument.getType(), InstrumentType.typesWithCoupon());
        checkIsAmountSufficient(portfolio, dto.getSymbol(), dto.getAmount());

        var operation = new Operation()
            .setType(OperationType.COUPON)
            .setInstrument(instrument)
            .setPortfolio(portfolio)
            .setPrice(dto.getPrice())
            .setAmount(dto.getAmount())
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return CouponDto.from(operation);
    }

    @Transactional
    @Override
    public TaxDto tax(@NotNull TaxDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        checkIsMoneySufficient(portfolio, dto.getValue());

        var operation = new Operation()
            .setAmount(1)
            .setPortfolio(portfolio)
            .setPrice(dto.getValue())
            .setType(OperationType.TAX)
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return TaxDto.from(operation);
    }

    @Transactional
    @Override
    public BondRedemptionDto bondRedemption(@NotNull BondRedemptionDto dto) {
        var portfolio = loadPortfolio(dto.getPortfolioName());
        var instrument = loadInstrument(dto.getSymbol());
        validateInstrumentType(instrument.getType(), InstrumentType.typesWithCoupon());
        var instrumentPosition = portfolio.getPositions().stream()
                .filter(position -> position.getInstrument().equals(instrument))
                .findFirst()
                .orElseThrow(() -> new InstrumentPositionNotFoundException(portfolio.getName(), dto.getSymbol()));

        var operation = new Operation()
            .setType(OperationType.BOND_REDEMPTION)
            .setInstrument(instrument)
            .setPortfolio(portfolio)
            .setPrice(instrument.getBondParValue())
            .setAmount(instrumentPosition.getAmount())
            .setAccumulatedCouponIncome(dto.getAccumulatedCouponIncome())
            .setWhenAdd(getWhenAdd(dto));
        operation = operationRepository.save(operation);

        processOperation(operation);
        return BondRedemptionDto.from(operation);
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

    private void checkIsAmountSufficient(Portfolio portfolio, String symbol, Integer amount) {
        if (portfolio.getPositions().stream()
                .filter(position -> position.getInstrument().getSymbol().equals(symbol))
                .anyMatch(position -> position.getAmount() < amount))
            throw new InsufficientAmountException(portfolio.getName(), symbol);
    }

    private void validateInstrumentType(InstrumentType type, Set<InstrumentType> availableTypes) {
        if (!availableTypes.contains(type))
            throw new UnsupportedInstrumentTypeException(type.name());
    }

    /** Process operation. */
    private void processOperation(Operation operation) {
        operation.setProcessed(true);
        var portfolio = operation.getPortfolio();
        switch (operation.getType()) {
            case ADD_MONEY:
            case DIVIDEND:
            case COUPON:
                portfolio.setAvailableMoney(portfolio.getAvailableMoney().add(operation.getTotalPrice()));
                break;
            case WITHDRAW_MONEY:
            case TAX:
                portfolio.setAvailableMoney(portfolio.getAvailableMoney().subtract(operation.getTotalPrice()));
                break;
            case BUY:
                portfolio.setAvailableMoney(portfolio.getAvailableMoney()
                        .subtract(operation.getTotalPrice())
                        .subtract(operation.getAccumulatedCouponIncome()));
                updateInstrumentPositionOnBuy(operation);
                break;
            case SELL:
            case BOND_REDEMPTION:
                portfolio.setAvailableMoney(portfolio.getAvailableMoney().add(operation.getTotalPrice())
                        .add(operation.getAccumulatedCouponIncome()));
                updateInstrumentPositionOnSell(operation);
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
            final var newAmount = position.getAmount() + operation.getAmount();
            position.setAmount(newAmount);
            position.setAccountingPrice(calcAccountingPrice(operation.getInstrument(), portfolio.getOperations(), newAmount));
        } else {
            // add new position: operation price === accounting price
            var newPosition = new InstrumentPosition()
                .setPortfolio(portfolio)
                .setInstrument(operation.getInstrument())
                .setAmount(operation.getAmount())
                .setAccountingPrice(operation.getPrice());
            portfolio.getPositions().add(newPosition);
        }
    }

    private void updateInstrumentPositionOnSell(Operation operation) {
        var portfolio = operation.getPortfolio();
        var position = portfolio.getPositions().stream()
                .filter(instrumentPosition -> instrumentPosition.getInstrument().equals(operation.getInstrument()))
                .findFirst()
                .orElseThrow(() -> new InstrumentPositionNotFoundException(portfolio.getName(),
                        operation.getInstrument().getSymbol()));

        final var newAmount = position.getAmount() - operation.getAmount();
        position.setAmount(newAmount);
        position.setAccountingPrice(newAmount > 0 ? calcAccountingPrice(operation.getInstrument(), portfolio.getOperations(), newAmount)
                : BigDecimal.ZERO);
    }

    /** Calc accounting price based on total values and current amount. */
    private BigDecimal calcAccountingPrice(Instrument instrument, List<Operation> operations, Integer currentAmount) {
        // calc new accounting price
        BigDecimal buyOperationsTotalValue = operations.stream()
                .filter(Operation::getProcessed)
                .filter(op -> !op.getDeleted())
                .filter(op -> op.getType() == OperationType.BUY)
                .filter(op -> op.getInstrument().equals(instrument))
                .map(Operation::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sellOperationsTotalValue = operations.stream()
                .filter(Operation::getProcessed)
                .filter(op -> !op.getDeleted())
                .filter(op -> op.getType() == OperationType.SELL)
                .filter(op -> op.getInstrument().equals(instrument))
                .map(Operation::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return buyOperationsTotalValue.subtract(sellOperationsTotalValue)
                .divide(BigDecimal.valueOf(currentAmount), applicationConfig.getBigdecimalOperationsScale(), RoundingMode.FLOOR);
    }

    /** Extract from DTO or get current LDT. */
    private LocalDateTime getWhenAdd(OperationDto dto) {
        return ofNullable(dto.getWhenAdd()).orElseGet(LocalDateTime::now);
    }
}
