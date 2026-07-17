package com.expensetracker.service;

import com.expensetracker.dto.BudgetDto;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    /**
     * Create or update a budget for a category
     */
    @Transactional
    public BudgetDto createOrUpdateBudget(Long categoryId, BigDecimal limitAmount, String username) {
        log.info("Creating/updating budget for category: {} for user: {}", categoryId, username);

        User user = userService.findByUsername(username);
        Category category = categoryService.getCategoryById(categoryId);
        LocalDate monthYear = LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1);

        // Check if budget already exists for this category and month
        Budget budget = budgetRepository
                .findByUserIdAndCategoryIdAndMonthYear(user.getId(), categoryId, monthYear)
                .orElse(null);

        if (budget == null) {
            // Create new budget
            budget = Budget.builder()
                    .user(user)
                    .category(category)
                    .monthYear(monthYear)
                    .limitAmount(limitAmount)
                    .build();
        } else {
            // Update existing budget
            budget.setLimitAmount(limitAmount);
        }

        Budget savedBudget = budgetRepository.save(budget);
        log.info("Budget saved: {}", savedBudget.getId());

        // Get spending for this category this month
        BigDecimal spent = expenseRepository.getCategoryMonthlyTotal(
                user.getId(), categoryId,
                monthYear.getYear(), monthYear.getMonthValue());

        return BudgetDto.fromEntityWithSpending(savedBudget, spent != null ? spent : BigDecimal.ZERO);
    }

    /**
     * Get all budgets for a user for the current month
     */
    public List<BudgetDto> getCurrentMonthBudgets(String username) {
        User user = userService.findByUsername(username);
        LocalDate monthYear = LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1);

        return getBudgetsForMonth(user.getId(), monthYear);
    }

    /**
     * Get all budgets for a user for a specific month
     */
    public List<BudgetDto> getBudgetsForMonth(Long userId, LocalDate monthYear) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
        List<BudgetDto> budgetDtos = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spent = expenseRepository.getCategoryMonthlyTotal(
                    userId,
                    budget.getCategory().getId(),
                    monthYear.getYear(),
                    monthYear.getMonthValue());

            budgetDtos.add(BudgetDto.fromEntityWithSpending(
                    budget,
                    spent != null ? spent : BigDecimal.ZERO));
        }

        return budgetDtos;
    }

    /**
     * Get a specific budget for a category and month
     */
    public BudgetDto getBudgetForCategory(Long categoryId, String username) {
        User user = userService.findByUsername(username);
        LocalDate monthYear = LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1);

        Budget budget = budgetRepository
                .findByUserIdAndCategoryIdAndMonthYear(user.getId(), categoryId, monthYear)
                .orElse(null);

        if (budget == null) {
            return null;
        }

        BigDecimal spent = expenseRepository.getCategoryMonthlyTotal(
                user.getId(), categoryId,
                monthYear.getYear(), monthYear.getMonthValue());

        return BudgetDto.fromEntityWithSpending(budget, spent != null ? spent : BigDecimal.ZERO);
    }

    /**
     * Delete a budget
     */
    @Transactional
    public void deleteBudget(Long budgetId, String username) {
        log.info("Deleting budget: {} for user: {}", budgetId, username);

        User user = userService.findByUsername(username);
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + budgetId));

        // Verify budget belongs to user
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        budgetRepository.delete(budget);
        log.info("Budget deleted: {}", budgetId);
    }

    /**
     * Get budgets that are exceeded
     */
    public List<BudgetDto> getExceededBudgets(String username) {
        User user = userService.findByUsername(username);
        LocalDate monthYear = LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1);

        List<Object[]> exceeded = budgetRepository.findExceededBudgets(user.getId(), monthYear);
        List<BudgetDto> exceededBudgets = new ArrayList<>();

        for (Object[] row : exceeded) {
            Budget budget = (Budget) row[0];
            BigDecimal spent = (BigDecimal) row[1];
            exceededBudgets.add(BudgetDto.fromEntityWithSpending(budget, spent));
        }

        return exceededBudgets;
    }

    /**
     * Check if a category has exceeded its budget
     */
    public boolean isBudgetExceeded(Long categoryId, String username) {
        BudgetDto budget = getBudgetForCategory(categoryId, username);
        if (budget == null) {
            return false;
        }
        return budget.getSpentAmount().compareTo(budget.getLimitAmount()) > 0;
    }

    /**
     * Get total budget limit for a user in the current month
     */
    public BigDecimal getTotalBudgetLimit(String username) {
        User user = userService.findByUsername(username);
        LocalDate monthYear = LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1);

        Double total = budgetRepository.getTotalBudgetLimit(user.getId(), monthYear);
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }

    /**
     * Get total spent vs total budget for a user
     */
    public BigDecimal getTotalBudgetUsage(String username) {
        User user = userService.findByUsername(username);
        LocalDate monthYear = LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1);

        // Get total spent for current month
        BigDecimal totalSpent = expenseRepository.getMonthlyTotal(
                user.getId(),
                monthYear.getYear(),
                monthYear.getMonthValue());

        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        BigDecimal totalBudget = getTotalBudgetLimit(username);

        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalSpent.divide(totalBudget, 2, RoundingMode.HALF_UP);
    }
}