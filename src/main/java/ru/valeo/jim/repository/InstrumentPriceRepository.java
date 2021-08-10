package ru.valeo.jim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.valeo.jim.domain.Instrument;
import ru.valeo.jim.domain.InstrumentPrice;

import java.time.LocalDateTime;
import java.util.List;

public interface InstrumentPriceRepository extends JpaRepository<InstrumentPrice, Long> {

    List<InstrumentPrice> findByInstrument(Instrument instrument);

    List<InstrumentPrice> findByWhenAddLessThanEqualOrderByWhenAddDesc(LocalDateTime whenAdd);
}
