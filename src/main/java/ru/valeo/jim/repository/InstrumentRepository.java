package ru.valeo.jim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.valeo.jim.domain.Instrument;

public interface InstrumentRepository extends JpaRepository<Instrument, String> {
}
