package ru.valeo.jim.dto.operation;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.Operation;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentConversionDto extends OperationDto {

    @NotBlank String symbol;
    Integer newAmount;

    @Builder
    public InstrumentConversionDto(String portfolioName, LocalDateTime whenAdd, @NotBlank String symbol,
                                   Integer newAmount) {
        super(portfolioName, whenAdd);
        this.symbol = symbol;
        this.newAmount = newAmount;
    }

    public static InstrumentConversionDto from(Operation operation) {
        var dto = new InstrumentConversionDto();
        dto.setPortfolioName(operation.getPortfolio().getName());
        dto.setSymbol(operation.getInstrument().getSymbol());
        dto.setNewAmount(operation.getAmount());
        dto.setWhenAdd(operation.getWhenAdd());
        return dto;
    }

    @Override
    public String toString() {
        return "Instrument conversion operation: " +
                "portfolioName='" + portfolioName + '\'' +
                ", symbol='" + symbol + '\'' +
                ", new amount='" + newAmount + '\'';
    }

}
