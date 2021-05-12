package ru.valeo.jim.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/** If we have some instrument in portfolio - we have position on this instrument. */
@Data
@Table(name = "instrument_position",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "instrument_id"}))
@Entity
public class InstrumentPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Min(0)
    @Column(name = "amount", nullable = false)
    private Integer amount = 0;

    @Min(0)
    @Column(name = "accounting_price", nullable = false)
    private BigDecimal accountingPrice;
}
