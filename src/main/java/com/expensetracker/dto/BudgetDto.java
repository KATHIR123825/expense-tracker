package com.expensetracker.dto;

import com.expensetracker.entity.Budget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDto {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private LocalDate monthYear;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private double percentageUsed;

    public static BudgetDto fromEntity(Budget budget) {
        return BudgetDto.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .monthYear(budget.getMonthYear())
                .limitAmount(budget.getLimitAmount())
                .spentAmount(BigDecimal.ZERO)
                .percentageUsed(0.0)
                .build();
    }

    public static BudgetDto fromEntityWithSpending(Budget budget, BigDecimal spentAmount) {
        BudgetDto dto = fromEntity(budget);
        dto.setSpentAmount(spentAmount);
        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            dto.setPercentageUsed(spentAmount.doubleValue() / budget.getLimitAmount().doubleValue() * 100);
        }
        return dto;
    }
}