package com.expensetracker.controller;

import com.expensetracker.dto.ExpenseDto;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String listExpenses(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("Expenses list requested for user: {}", username);

            List<ExpenseDto> expenses = expenseService.getAllExpensesForUser(username);
            model.addAttribute("expenses", expenses);
            model.addAttribute("username", username);

            // Get categories for filter
            User user = userService.findByUsername(username);
            List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());
            model.addAttribute("categories", categories);

            return "expenses/list";
        } catch (Exception e) {
            log.error("Error loading expenses: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading expenses: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/add")
    public String addExpenseForm(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("Add expense form requested for user: {}", username);

            User user = userService.findByUsername(username);
            List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());

            model.addAttribute("expense", new ExpenseDto());
            model.addAttribute("categories", categories);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("username", username);

            return "expenses/add";
        } catch (Exception e) {
            log.error("Error loading add expense form: {}", e.getMessage(), e);
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/add")
    public String addExpense(@ModelAttribute ExpenseDto expenseDto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("Add expense submitted for user: {}", username);

            // Set date if not provided
            if (expenseDto.getDate() == null) {
                expenseDto.setDate(LocalDate.now());
            }

            expenseService.createExpense(expenseDto, username);
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Error adding expense: {}", e.getMessage(), e);
            return "redirect:/expenses/add?error=" + e.getMessage();
        }
    }

    @GetMapping("/edit/{id}")
    public String editExpenseForm(@PathVariable Long id, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("Edit expense form requested for expense: {} by user: {}", id, username);

            ExpenseDto expense = expenseService.getExpenseById(id, username);
            User user = userService.findByUsername(username);
            List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());

            model.addAttribute("expense", expense);
            model.addAttribute("categories", categories);
            model.addAttribute("username", username);

            return "expenses/edit";
        } catch (Exception e) {
            log.error("Error loading edit expense form: {}", e.getMessage(), e);
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/edit/{id}")
    public String editExpense(@PathVariable Long id, @ModelAttribute ExpenseDto expenseDto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("Edit expense submitted for expense: {} by user: {}", id, username);

            expenseService.updateExpense(id, expenseDto, username);
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Error updating expense: {}", e.getMessage(), e);
            return "redirect:/expenses/edit/" + id + "?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("Delete expense requested for expense: {} by user: {}", id, username);

            expenseService.deleteExpense(id, username);
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Error deleting expense: {}", e.getMessage(), e);
            return "redirect:/expenses?error=" + e.getMessage();
        }
    }

    @GetMapping("/filter")
    public String filterExpenses(@RequestParam(required = false) LocalDate startDate,
                                 @RequestParam(required = false) LocalDate endDate,
                                 @RequestParam(required = false) Long categoryId,
                                 Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("Filter expenses requested for user: {}", username);

            List<ExpenseDto> expenses;

            if (startDate != null && endDate != null) {
                expenses = expenseService.getExpensesBetweenDates(username, startDate, endDate);
            } else if (categoryId != null) {
                expenses = expenseService.getExpensesByCategory(username, categoryId);
            } else {
                expenses = expenseService.getAllExpensesForUser(username);
            }

            User user = userService.findByUsername(username);
            List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());

            model.addAttribute("expenses", expenses);
            model.addAttribute("categories", categories);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("selectedCategory", categoryId);
            model.addAttribute("username", username);

            return "expenses/list";
        } catch (Exception e) {
            log.error("Error filtering expenses: {}", e.getMessage(), e);
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error";
        }
    }
}