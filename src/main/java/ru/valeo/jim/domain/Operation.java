package ru.valeo.jim.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "operation")
@Entity
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OperationType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Min(1)
    @Column(name = "amount", nullable = false)
    private Integer amount = 1;

    @Min(0)
    @Column(name = "price", nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "when_add", nullable = false)
    private LocalDateTime whenAdd;

    public BigDecimal getTotalPrice() { return price.multiply(BigDecimal.valueOf(amount)); }
}
