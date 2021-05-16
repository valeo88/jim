package ru.valeo.jim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.valeo.jim.domain.Operation;

public interface OperationRepository extends JpaRepository<Operation, Long> {
}
