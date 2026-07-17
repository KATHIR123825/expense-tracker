package com.expensetracker.dto;

import com.expensetracker.entity.Expense;
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
public class ExpenseDto {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String paymentMethod;
    private String categoryName;
    private Long categoryId;
    private String notes;

    public static ExpenseDto fromEntity(Expense expense) {
        if (expense == null) {
            return null;
        }

        ExpenseDto dto = ExpenseDto.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .paymentMethod(expense.getPaymentMethod() != null ? expense.getPaymentMethod().name() : "OTHER")
                .notes(expense.getNotes())
                .build();

        if (expense.getCategory() != null) {
            dto.setCategoryName(expense.getCategory().getName());
            dto.setCategoryId(expense.getCategory().getId());
        }

        return dto;
    }
}