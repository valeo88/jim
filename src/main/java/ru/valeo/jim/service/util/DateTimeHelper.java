package ru.valeo.jim.service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.valeo.jim.config.ApplicationConfig;

import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Component
public class DateTimeHelper {

    private final ApplicationConfig applicationConfig;

    public LocalDateTime parse(String source) {
        return ofNullable(source)
                .filter(StringUtils::hasText)
                .map(text -> LocalDateTime.parse(text, applicationConfig.getOperationWhenAddFormatter()))
                .orElse(null);
    }

    public String ldtToString(LocalDateTime source) {
        return applicationConfig.getOperationWhenAddFormatter().format(source);
    }
}
