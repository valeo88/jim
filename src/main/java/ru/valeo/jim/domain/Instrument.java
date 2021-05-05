package ru.valeo.jim.domain;

import lombok.Data;

import javax.persistence.*;

/** Financial instrument: share, bond, ETF, ... */
@Data
@Table(name = "instrument")
@Entity
public class Instrument {
    @Id
    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InstrumentType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private InstrumentCategory category;

    @Column(name = "isin")
    private String isin;

}
