package ru.valeo.jim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.valeo.jim.domain.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, String> {
}
