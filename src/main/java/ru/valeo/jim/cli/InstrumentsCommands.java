package ru.valeo.jim.cli;

import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.valeo.jim.dto.InstrumentDto;
import ru.valeo.jim.service.InstrumentsService;

import java.util.stream.Collectors;

@AllArgsConstructor
@ShellComponent
public class InstrumentsCommands {

    private final InstrumentsService instrumentsService;

    @ShellMethod(value = "Print all available financial instruments", key = "instruments")
    public String printInstruments() {
        return instrumentsService.getInstruments().stream()
                .map(InstrumentDto::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
