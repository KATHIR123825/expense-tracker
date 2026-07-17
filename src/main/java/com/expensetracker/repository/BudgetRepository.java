package com.expensetracker.repository;

import com.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // ============================================================
    // BASIC QUERIES
    // ============================================================

    /**
     * Find all budgets for a user
     */
    List<Budget> findByUserId(Long userId);

    /**
     * Find all budgets for a user in a specific month
     */
    List<Budget> findByUserIdAndMonthYear(Long userId, LocalDate monthYear);

    /**
     * Find all budgets for a user in a specific month and year
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND YEAR(b.monthYear) = :year AND MONTH(b.monthYear) = :month")
    List<Budget> findByUserAndMonth(@Param("userId") Long userId,
                                    @Param("year") int year,
                                    @Param("month") int month);

    // ============================================================
    // CATEGORY BUDGET QUERIES
    // ============================================================

    /**
     * Find budget for a specific user, category, and month
     */
    Optional<Budget> findByUserIdAndCategoryIdAndMonthYear(Long userId, Long categoryId, LocalDate monthYear);

    /**
     * Check if budget exists for a specific user, category, and month
     */
    boolean existsByUserIdAndCategoryIdAndMonthYear(Long userId, Long categoryId, LocalDate monthYear);

    /**
     * Find all budgets for a user that are active in a specific month
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.monthYear = :monthYear ORDER BY b.category.name ASC")
    List<Budget> findAllByUserIdAndMonthYearOrderByCategoryNameAsc(@Param("userId") Long userId,
                                                                   @Param("monthYear") LocalDate monthYear);

    // ============================================================
    // BUDGET VS SPENDING QUERIES
    // ============================================================

    /**
     * Get budgets with spending for a user in a month
     */
    @Query("SELECT b, COALESCE(SUM(e.amount), 0) as spent " +
            "FROM Budget b " +
            "LEFT JOIN Expense e ON e.category.id = b.category.id " +
            "AND e.user.id = b.user.id " +
            "AND YEAR(e.date) = YEAR(b.monthYear) " +
            "AND MONTH(e.date) = MONTH(b.monthYear) " +
            "WHERE b.user.id = :userId AND b.monthYear = :monthYear " +
            "GROUP BY b.id")
    List<Object[]> getBudgetsWithSpending(@Param("userId") Long userId,
                                          @Param("monthYear") LocalDate monthYear);

    /**
     * Get total budget limit for a user in a month
     */
    @Query("SELECT SUM(b.limitAmount) FROM Budget b WHERE b.user.id = :userId AND b.monthYear = :monthYear")
    Double getTotalBudgetLimit(@Param("userId") Long userId,
                               @Param("monthYear") LocalDate monthYear);

    /**
     * Find budgets where spending exceeds limit
     * FIXED: Using the full expression instead of alias 'spent'
     */
    @Query("SELECT b, COALESCE(SUM(e.amount), 0) as spent " +
            "FROM Budget b " +
            "LEFT JOIN Expense e ON e.category.id = b.category.id " +
            "AND e.user.id = b.user.id " +
            "AND YEAR(e.date) = YEAR(b.monthYear) " +
            "AND MONTH(e.date) = MONTH(b.monthYear) " +
            "WHERE b.user.id = :userId " +
            "AND b.monthYear = :monthYear " +
            "GROUP BY b.id " +
            "HAVING COALESCE(SUM(e.amount), 0) > b.limitAmount")
    List<Object[]> findExceededBudgets(@Param("userId") Long userId,
                                       @Param("monthYear") LocalDate monthYear);

    // ============================================================
    // DELETE QUERIES
    // ============================================================

    /**
     * Delete all budgets for a specific user and month
     */
    void deleteByUserIdAndMonthYear(Long userId, LocalDate monthYear);

    /**
     * Delete all budgets for a specific category
     */
    void deleteByCategoryId(Long categoryId);
}