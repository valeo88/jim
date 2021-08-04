package ru.valeo.jim.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.InstrumentPrice;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class InstrumentPriceDto {

    @NotBlank protected String symbol;
    @NotBlank protected String name;
    protected String baseCurrencyCode;
    protected String type;
    protected String categoryCode;
    @NotNull @Min(0) BigDecimal price;
    @NotNull @Min(0) BigDecimal accumulatedCouponIncome = BigDecimal.ZERO;
    LocalDateTime whenAdd;

    public static InstrumentPriceDto from(InstrumentPrice instrumentPrice) {
        var dto = new InstrumentPriceDto();
        dto.setSymbol(instrumentPrice.getInstrument().getSymbol());
        dto.setName(instrumentPrice.getInstrument().getName());
        dto.setBaseCurrencyCode(instrumentPrice.getInstrument().getBaseCurrency().getCode());
        dto.setType(instrumentPrice.getInstrument().getType().toString());
        dto.setCategoryCode(instrumentPrice.getInstrument().getCategory().getCode());
        dto.setPrice(instrumentPrice.getPrice());
        dto.setAccumulatedCouponIncome(instrumentPrice.getAccumulatedCouponIncome());
        dto.setWhenAdd(instrumentPrice.getWhenAdd());

        return dto;
    }

    @Override
    public String toString() {
        return "Instrument price: " +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", categoryCode='" + categoryCode + '\'' +
                ", price=" + price + baseCurrencyCode +
                ", accumulatedCouponIncome=" + accumulatedCouponIncome + baseCurrencyCode +
                ", date=" + whenAdd;
    }
}
