package ru.valeo.jim.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** History of instrument prices. */
@Accessors(chain = true)
@Data
@Table(name = "instrument_price")
@Entity
public class InstrumentPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Min(0)
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    /** Used for bonds. */
    @Min(0)
    @Column(name = "accumulated_coupon_income", nullable = false)
    private BigDecimal accumulatedCouponIncome = BigDecimal.ZERO;

    @Column(name = "when_add", nullable = false)
    private LocalDateTime whenAdd;
}
