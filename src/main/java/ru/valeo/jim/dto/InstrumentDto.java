package ru.valeo.jim.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.Instrument;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class InstrumentDto {

    @NotBlank protected String symbol;
    @NotBlank protected String name;
    @NotBlank protected String baseCurrencyCode;
    @NotBlank protected String type;
    @NotBlank protected String categoryCode;
    protected String categoryName;
    protected String isin;

    public static InstrumentDto from(Instrument instrument) {
        var dto = new InstrumentDto();
        dto.setSymbol(instrument.getSymbol());
        dto.setName(instrument.getName());
        dto.setIsin(instrument.getIsin());
        dto.setBaseCurrencyCode(instrument.getBaseCurrency().getCode());
        dto.setType(instrument.getType().name());
        dto.setCategoryCode(instrument.getCategory().getCode());
        dto.setCategoryName(instrument.getCategory().getName());

        return dto;
    }

    @Override
    public String toString() {
        return "Instrument: " +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", baseCurrencyCode='" + baseCurrencyCode + '\'' +
                ", type='" + type + '\'' +
                ", categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", isin='" + isin + '\'';
    }
}
