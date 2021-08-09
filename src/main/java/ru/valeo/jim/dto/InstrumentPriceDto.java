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

    @NotBlank private String symbol;
    @NotBlank private String name;
    private String baseCurrencyCode;
    private String type;
    private String categoryCode;
    @NotNull @Min(0) private BigDecimal price;
    @NotNull @Min(0) private BigDecimal accumulatedCouponIncome = BigDecimal.ZERO;
    private LocalDateTime whenAdd;
    private boolean isPercent = false;

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
                "date=" + whenAdd +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", price=" + price + baseCurrencyCode +
                ", accumulatedCouponIncome=" + accumulatedCouponIncome + baseCurrencyCode;
    }
}
