package ru.valeo.jim.domain;

import ru.valeo.jim.exception.InstrumentTypeNotFoundException;

import java.util.Arrays;
import java.util.Set;

/** Type of financial instrument. */
public enum InstrumentType {
    SHARE,
    BOND,
    ETF;

    public static InstrumentType findByName(String name) {
        return Arrays.stream(values())
                .filter(instrumentType -> instrumentType.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new InstrumentTypeNotFoundException(name));
    }

    public static Set<InstrumentType> typesWithCoupon() {
        return Set.of(BOND);
    }

    public static Set<InstrumentType> typesWithDividend() {
        return Set.of(SHARE);
    }
}
