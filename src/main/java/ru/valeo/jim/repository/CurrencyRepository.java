package ru.valeo.jim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.valeo.jim.domain.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
}
