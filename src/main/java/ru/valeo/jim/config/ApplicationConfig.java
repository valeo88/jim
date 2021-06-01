package ru.valeo.jim.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ApplicationConfig {
    private String defaultPortfolioName;
    private int bigdecimalOperationsScale = 3;
}
