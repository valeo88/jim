package ru.valeo.jim.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** Category of financial instrument.
 * Used for custom categorization and inside portfolio distribution.*/
@Data
@Entity
@Table(name = "instrument_category")
public class InstrumentCategory {

    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}
