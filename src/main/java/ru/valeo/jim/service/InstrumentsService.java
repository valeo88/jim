package ru.valeo.jim.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.repository.InstrumentRepository;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class InstrumentsService {

    private final InstrumentRepository instrumentRepository;

    public List<InstrumentDto> getInstruments() {
        return instrumentRepository.findAll().stream().map(InstrumentDto::from).collect(Collectors.toList());
    }
}
