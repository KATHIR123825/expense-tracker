package com.expensetracker.controller;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String listCategories(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.info("Categories list requested for user: {}", username);

        User user = userService.findByUsername(username);
        List<Category> categories = categoryService.getAllCategoriesForUser(user.getId());
        List<Category> predefined = categoryService.getPredefinedCategories();
        List<Category> custom = categoryService.getUserCategories(user.getId());

        model.addAttribute("categories", categories);
        model.addAttribute("predefinedCategories", predefined);
        model.addAttribute("customCategories", custom);
        model.addAttribute("username", username);

        return "categories/list";
    }

    @GetMapping("/add")
    public String addCategoryForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.info("Add category form requested for user: {}", username);

        model.addAttribute("category", new Category());
        model.addAttribute("username", username);

        return "categories/add";
    }

    @PostMapping("/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam String description,
                              Authentication authentication) {
        String username = authentication.getName();
        log.info("Add category submitted for user: {}", username);

        User user = userService.findByUsername(username);
        categoryService.createCustomCategory(name, description, user);

        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 Authentication authentication) {
        String username = authentication.getName();
        log.info("Delete category requested for category: {} by user: {}", id, username);

        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}