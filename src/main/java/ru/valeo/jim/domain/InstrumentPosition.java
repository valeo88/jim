package ru.valeo.jim.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;

import lombok.Data;
import lombok.experimental.Accessors;

/** If we have some instrument in portfolio - we have position on this instrument. */
@Accessors(chain = true)
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

    @Column(name = "exclude_from_distribution", nullable = false)
    private Boolean excludeFromDistribution = false;
}
