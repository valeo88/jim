package ru.valeo.jim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.valeo.jim.domain.InstrumentCategory;

public interface InstrumentCategoryRepository extends JpaRepository<InstrumentCategory, String> {
}
