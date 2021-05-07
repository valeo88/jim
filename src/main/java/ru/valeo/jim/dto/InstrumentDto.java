package ru.valeo.jim.dto;

import lombok.Data;
import ru.valeo.jim.domain.Instrument;

import javax.validation.constraints.NotBlank;

@Data
public class InstrumentDto {

    @NotBlank String symbol;
    @NotBlank String name;
    @NotBlank String baseCurrencyCode;
    @NotBlank String type;
    @NotBlank String categoryCode;
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
