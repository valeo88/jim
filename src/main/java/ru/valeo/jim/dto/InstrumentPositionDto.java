package ru.valeo.jim.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.InstrumentPosition;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class InstrumentPositionDto {

    @NotBlank protected String portfolioName;
    @NotBlank protected String symbol;
    @NotBlank protected String name;
    @NotBlank protected String baseCurrencyCode;
    @NotBlank protected String type;
    @NotBlank protected String categoryCode;
    protected String categoryName;
    protected String isin;
    @NotNull @Min(1) Integer amount;
    @NotNull @Min(0) BigDecimal accountingPrice;
    Boolean excludedFromDistribution;

    public static InstrumentPositionDto from(InstrumentPosition position) {
        var dto = new InstrumentPositionDto();
        dto.setPortfolioName(position.getPortfolio().getName());
        dto.setSymbol(position.getInstrument().getSymbol());
        dto.setName(position.getInstrument().getName());
        dto.setBaseCurrencyCode(position.getInstrument().getBaseCurrency().getCode());
        dto.setType(position.getInstrument().getType().name());
        dto.setCategoryCode(position.getInstrument().getCategory().getCode());
        dto.setCategoryName(position.getInstrument().getCategory().getName());
        dto.setIsin(position.getInstrument().getIsin());
        dto.setAmount(position.getAmount());
        dto.setAccountingPrice(position.getAccountingPrice());
        dto.setExcludedFromDistribution(position.getExcludeFromDistribution());

        return dto;
    }

    @Override
    public String toString() {
        return "Instrument position:" +
                "portfolioName='" + portfolioName + '\'' +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", isin='" + isin + '\'' +
                ", amount=" + amount +
                ", accountingPrice=" + accountingPrice + "" + baseCurrencyCode +
                ", excludedFromDistribution=" + excludedFromDistribution;
    }
}
