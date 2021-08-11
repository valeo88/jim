package ru.valeo.jim.domain;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/** Target instrument categories distribution in portfolio.
 * Example: shares - 70%, government bonds - 30%. */
@Data
@Entity
@Table(name = "instrument_category_target_distribution",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "category_id"}))
public class InstrumentCategoryTargetDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private InstrumentCategory category;

    @Column(name = "percent", nullable = false)
    private BigDecimal percent;
}
