package com.expensetracker.service;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Category.CategoryType;
import com.expensetracker.entity.User;
import com.expensetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all categories available for a user (predefined + user's own)
     */
    public List<Category> getAllCategoriesForUser(Long userId) {
        return categoryRepository.findAllAvailableForUser(userId);
    }

    /**
     * Get all predefined categories
     */
    public List<Category> getPredefinedCategories() {
        return categoryRepository.findByTypeAndUserIsNull(CategoryType.PREDEFINED);
    }

    /**
     * Get user's custom categories
     */
    public List<Category> getUserCategories(Long userId) {
        return categoryRepository.findByUser_IdAndType(userId, CategoryType.USER_CREATED);
    }

    /**
     * Create a custom category for a user
     */
    @Transactional
    public Category createCustomCategory(String name, String description, User user) {
        log.info("Creating custom category: {} for user: {}", name, user.getUsername());

        // Check if category already exists for this user
        categoryRepository.findByNameIgnoreCaseAndUserIdOrNull(name, user.getId())
                .ifPresent(c -> {
                    throw new RuntimeException("Category already exists: " + name);
                });

        Category category = Category.builder()
                .name(name)
                .description(description)
                .type(CategoryType.USER_CREATED)
                .user(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Custom category created: {}", savedCategory.getName());
        return savedCategory;
    }

    /**
     * Get category by ID
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    /**
     * Delete a category
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category: {}", id);
        Category category = getCategoryById(id);

        // Only allow deletion of user-created categories
        if (category.getType() == CategoryType.PREDEFINED) {
            throw new RuntimeException("Cannot delete predefined categories");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {}", id);
    }
}