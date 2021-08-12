package ru.valeo.jim.dto;

import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class PortfolioRebalancePropositionDto {

    @NotBlank private String portfolioName;
    private List<RebalanceOperation> operations = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    public static class RebalanceOperation {
        private String categoryCode;
        private String categoryName;
        private String operation;
        private BigDecimal sum;
    }
}
