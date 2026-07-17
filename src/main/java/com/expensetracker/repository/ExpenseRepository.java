package com.expensetracker.repository;
import com.expensetracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ============================================================
    // BASIC QUERIES
    // ============================================================

    /**
     * Find all expenses for a user
     */
    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    /**
     * Find all expenses for a user with pagination
     */
    Page<Expense> findByUserId(Long userId, Pageable pageable);

    /**
     * Find expenses by user and category
     */
    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    // ============================================================
    // DATE RANGE QUERIES
    // ============================================================

    /**
     * Find expenses for a user between two dates
     */
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses for a user and category between two dates
     */
    List<Expense> findByUserIdAndCategoryIdAndDateBetween(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses for a user on a specific date
     */
    List<Expense> findByUserIdAndDate(Long userId, LocalDate date);

    // ============================================================
    // SUMMARY & AGGREGATION QUERIES
    // ============================================================

    /**
     * Get total expenses for a user
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalExpenses(@Param("userId") Long userId);

    /**
     * Get total expenses for a user between dates
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesBetweenDates(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Get total expenses by category for a user in a date range
     */
    @Query("SELECT e.category.id, e.category.name, SUM(e.amount) as total " +
            "FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category.id, e.category.name " +
            "ORDER BY total DESC")
    List<Object[]> getExpensesByCategory(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * Get monthly total for a user
     */
    @Query("SELECT SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal getMonthlyTotal(@Param("userId") Long userId,
                               @Param("year") int year,
                               @Param("month") int month);

    // ============================================================
    // PAYMENT METHOD QUERIES
    // ============================================================

    /**
     * Get expenses by payment method for a user
     */
    List<Expense> findByUserIdAndPaymentMethod(Long userId, Expense.PaymentMethod paymentMethod);

    // ============================================================
    // BUDGET & WARNING QUERIES
    // ============================================================

    /**
     * Get expenses for a user and category in a month
     */
    @Query("SELECT SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.category.id = :categoryId " +
            "AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal getCategoryMonthlyTotal(@Param("userId") Long userId,
                                       @Param("categoryId") Long categoryId,
                                       @Param("year") int year,
                                       @Param("month") int month);

    // ============================================================
    // SEARCH QUERIES
    // ============================================================

    /**
     * Search expenses by description (case insensitive)
     */
    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Expense> searchByDescription(@Param("userId") Long userId,
                                      @Param("searchTerm") String searchTerm);

    /**
     * Search expenses by description and date range
     */
    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> searchByDescriptionAndDateRange(@Param("userId") Long userId,
                                                  @Param("searchTerm") String searchTerm,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // ============================================================
    // RECENT EXPENSES
    // ============================================================

    /**
     * Get recent expenses for a user (last n expenses)
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId ORDER BY e.date DESC, e.createdAt DESC")
    List<Expense> findRecentExpenses(@Param("userId") Long userId, Pageable pageable);

    /**
     * Get expenses for the current month for a user
     */
    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND YEAR(e.date) = :year AND MONTH(e.date) = :month " +
            "ORDER BY e.date DESC")
    List<Expense> getMonthlyExpenses(@Param("userId") Long userId,
                                     @Param("year") int year,
                                     @Param("month") int month);
}