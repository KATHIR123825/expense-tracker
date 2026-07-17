package com.expensetracker.controller;

import com.expensetracker.dto.BudgetDto;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String listBudgets(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.info("Budgets list requested for user: {}", username);

        List<BudgetDto> budgets = budgetService.getCurrentMonthBudgets(username);
        List<BudgetDto> exceededBudgets = budgetService.getExceededBudgets(username);
        User user = userService.findByUsername(username);
        List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());

        model.addAttribute("budgets", budgets);
        model.addAttribute("exceededBudgets", exceededBudgets);
        model.addAttribute("categories", categories);
        model.addAttribute("username", username);

        return "budgets/list";
    }

    @GetMapping("/add")
    public String addBudgetForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.info("Add budget form requested for user: {}", username);

        User user = userService.findByUsername(username);
        List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());

        model.addAttribute("budget", new BudgetDto());
        model.addAttribute("categories", categories);
        model.addAttribute("username", username);

        return "budgets/add";
    }

    @PostMapping("/add")
    public String addBudget(@RequestParam Long categoryId,
                            @RequestParam BigDecimal limitAmount,
                            Authentication authentication) {
        String username = authentication.getName();
        log.info("Add budget submitted for user: {}", username);

        budgetService.createOrUpdateBudget(categoryId, limitAmount, username);
        return "redirect:/budgets";
    }

    @GetMapping("/edit/{id}")
    public String editBudgetForm(@PathVariable Long id,
                                 Authentication authentication,
                                 Model model) {
        String username = authentication.getName();
        log.info("Edit budget form requested for budget: {} by user: {}", id, username);

        BudgetDto budget = budgetService.getCurrentMonthBudgets(username)
                .stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);

        User user = userService.findByUsername(username);
        List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());

        model.addAttribute("budget", budget);
        model.addAttribute("categories", categories);
        model.addAttribute("username", username);

        return "budgets/edit";
    }

    @PostMapping("/edit/{id}")
    public String editBudget(@PathVariable Long id,
                             @RequestParam BigDecimal limitAmount,
                             Authentication authentication) {
        String username = authentication.getName();
        log.info("Edit budget submitted for budget: {} by user: {}", id, username);

        BudgetDto existingBudget = budgetService.getCurrentMonthBudgets(username)
                .stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (existingBudget != null) {
            budgetService.createOrUpdateBudget(existingBudget.getCategoryId(), limitAmount, username);
        }

        return "redirect:/budgets";
    }

    @GetMapping("/delete/{id}")
    public String deleteBudget(@PathVariable Long id,
                               Authentication authentication) {
        String username = authentication.getName();
        log.info("Delete budget requested for budget: {} by user: {}", id, username);

        budgetService.deleteBudget(id, username);
        return "redirect:/budgets";
    }
}