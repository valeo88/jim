package ru.valeo.jim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@ConfigurationProperties("app")
@Data
@Component
public class ApplicationConfig {
    private String defaultPortfolioName;
    private int bigdecimalOperationsScale = 3;
    private String operationWhenAddFormat = "yyyy-MM-dd HH:mm:ss";
    private String defaultDateFormat = "yyyy-MM-dd";

    public DateTimeFormatter getOperationWhenAddFormatter() {
        return DateTimeFormatter.ofPattern(operationWhenAddFormat);
    }

    public DateTimeFormatter getDefaultDateFormatter() {
        return DateTimeFormatter.ofPattern(defaultDateFormat);
    }
}
