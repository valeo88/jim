package ru.valeo.jim.dto;

import lombok.Data;
import ru.valeo.jim.domain.Instrument;

@Data
public class InstrumentDto {
    String symbol;
    String name;
    String baseCurrencyCode;
    String type;
    String categoryCode;
    String categoryName;
    String isin;

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
}
