package com.expensetracker.controller;

import com.expensetracker.dto.BudgetDto;
import com.expensetracker.dto.ExpenseDto;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is logged in
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getName().equals("anonymousUser")) {
            log.info("Anonymous user accessing dashboard, redirecting to login");
            return "redirect:/login";
        }

        String username = authentication.getName();
        log.info("Dashboard requested for user: {}", username);

        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        try {
            List<ExpenseDto> monthlyExpenses = expenseService.getExpensesBetweenDates(username, startDate, endDate);
            BigDecimal totalExpenses = expenseService.getCurrentMonthTotal(username);
            List<BudgetDto> budgets = budgetService.getCurrentMonthBudgets(username);
            List<BudgetDto> exceededBudgets = budgetService.getExceededBudgets(username);
            BigDecimal totalBudgetLimit = budgetService.getTotalBudgetLimit(username);

            BigDecimal budgetUsage = BigDecimal.ZERO;
            if (totalBudgetLimit.compareTo(BigDecimal.ZERO) > 0) {
                budgetUsage = totalExpenses.divide(totalBudgetLimit, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
            }

            List<ExpenseDto> recentExpenses = expenseService.getRecentExpenses(username, 5);

            model.addAttribute("username", username);
            model.addAttribute("monthlyExpenses", monthlyExpenses);
            model.addAttribute("totalExpenses", totalExpenses);
            model.addAttribute("budgets", budgets);
            model.addAttribute("exceededBudgets", exceededBudgets);
            model.addAttribute("totalBudgetLimit", totalBudgetLimit);
            model.addAttribute("budgetUsage", budgetUsage);
            model.addAttribute("recentExpenses", recentExpenses);
            model.addAttribute("currentMonth", currentMonth.getMonth().toString() + " " + currentMonth.getYear());
        } catch (Exception e) {
            log.error("Error loading dashboard: {}", e.getMessage());
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
        }

        return "dashboard/index";
    }
}