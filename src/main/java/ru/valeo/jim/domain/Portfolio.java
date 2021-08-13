package ru.valeo.jim.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Accessors(chain = true)
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "portfolio", orphanRemoval = true)
    private List<InstrumentPosition> positions;

    @OneToMany(mappedBy = "portfolio")
    private List<Operation> operations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "portfolio")
    private List<InstrumentCategoryTargetDistribution> categoryTargetDistributions;
}
