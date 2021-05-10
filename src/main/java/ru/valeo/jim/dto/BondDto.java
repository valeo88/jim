package ru.valeo.jim.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.valeo.jim.domain.Instrument;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class BondDto extends InstrumentDto {

    BigDecimal bondParValue;

    public static BondDto from(Instrument instrument) {
        var dto = new BondDto();
        dto.setSymbol(instrument.getSymbol());
        dto.setName(instrument.getName());
        dto.setIsin(instrument.getIsin());
        dto.setBaseCurrencyCode(instrument.getBaseCurrency().getCode());
        dto.setType(instrument.getType().name());
        dto.setCategoryCode(instrument.getCategory().getCode());
        dto.setCategoryName(instrument.getCategory().getName());
        dto.setBondParValue(instrument.getBondParValue());

        return dto;
    }
}
