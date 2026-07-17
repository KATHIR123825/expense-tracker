package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Category.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all categories for a specific user (including predefined)
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL")
    List<Category> findAllByUserIdOrNull(@Param("userId") Long userId);

    /**
     * Find all predefined categories
     */
    List<Category> findByTypeAndUserIsNull(CategoryType type);

    /**
     * Find all user-created categories
     */
    List<Category> findByUser_IdAndType(Long userId, CategoryType type);

    /**
     * Find categories by name (case insensitive)
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByNameIgnoreCaseAndUserIdOrNull(@Param("name") String name, @Param("userId") Long userId);

    /**
     * Find all categories for a user (including predefined) sorted by name
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") Long userId);

    /**
     * Count categories by type for a user
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.user.id = :userId AND c.type = :type")
    long countByUserAndType(@Param("userId") Long userId, @Param("type") CategoryType type);

    /**
     * Find predefined categories by name
     */
    List<Category> findByTypeAndNameContainingIgnoreCase(CategoryType type, String name);
}