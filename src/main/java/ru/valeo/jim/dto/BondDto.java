package ru.valeo.jim.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.valeo.jim.domain.Instrument;
import ru.valeo.jim.domain.InstrumentType;
import ru.valeo.jim.exception.InstrumentDtoCastException;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public final class BondDto extends InstrumentDto {

    @NotNull private BigDecimal parValue;
    private String type = InstrumentType.BOND.name();

    public static BondDto from(Instrument instrument) {
        if (InstrumentType.BOND != instrument.getType())
            throw new InstrumentDtoCastException(BondDto.class);

        var dto = new BondDto();
        dto.setSymbol(instrument.getSymbol());
        dto.setName(instrument.getName());
        dto.setIsin(instrument.getIsin());
        dto.setBaseCurrencyCode(instrument.getBaseCurrency().getCode());
        dto.setType(instrument.getType().name());
        dto.setCategoryCode(instrument.getCategory().getCode());
        dto.setCategoryName(instrument.getCategory().getName());
        dto.setParValue(instrument.getBondParValue());

        return dto;
    }

    @Override
    public String toString() {
        return "Bond: " +
                "symbol='" + this.symbol + '\'' +
                ", name='" + name + '\'' +
                ", baseCurrencyCode='" + baseCurrencyCode + '\'' +
                ", type='" + type + '\'' +
                ", categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", isin='" + isin + '\'' +
                ", parValue='" + parValue + '\'';
    }
}
