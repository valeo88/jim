package ru.valeo.jim.service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.valeo.jim.config.ApplicationConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Component
public class DateTimeHelper {

    private final ApplicationConfig applicationConfig;

    public LocalDateTime parse(String source) {
        return ofNullable(source)
                .filter(StringUtils::hasText)
                .map(text -> {
                    try {
                        return LocalDateTime.parse(text, applicationConfig.getOperationWhenAddFormatter());
                    } catch (DateTimeParseException ignored) {
                        return LocalDateTime.parse(text, applicationConfig.getDefaultDateFormatter());
                    }
                })
                .orElse(null);
    }

    public String ldtToString(LocalDateTime source) {
        return applicationConfig.getOperationWhenAddFormatter().format(source);
    }
}
