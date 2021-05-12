package ru.valeo.jim.domain;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Table(name = "portfolio")
@Entity
public class Portfolio {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "available_money")
    private BigDecimal availableMoney = BigDecimal.ZERO;
}
