package com.expensetracker.service;

import com.expensetracker.dto.ExpenseDto;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.Expense.PaymentMethod;
import com.expensetracker.entity.User;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    @Transactional
    public ExpenseDto createExpense(ExpenseDto dto, String username) {
        log.info("Creating expense for user: {}", username);

        User user = userService.findByUsername(username);
        Category category = categoryService.getCategoryById(dto.getCategoryId());

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .paymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod()))
                .notes(dto.getNotes())
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense created with ID: {}", savedExpense.getId());

        return ExpenseDto.fromEntity(savedExpense);
    }

    public List<ExpenseDto> getAllExpensesForUser(String username) {
        User user = userService.findByUsername(username);
        return expenseRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(ExpenseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<ExpenseDto> getExpensesForUser(String username, int page, int size) {
        User user = userService.findByUsername(username);
        Pageable pageable = PageRequest.of(page, size);
        return expenseRepository.findByUserId(user.getId(), pageable)
                .map(ExpenseDto::fromEntity);
    }

    public List<ExpenseDto> getExpensesBetweenDates(String username, LocalDate startDate, LocalDate endDate) {
        User user = userService.findByUsername(username);
        return expenseRepository.findByUserIdAndDateBetween(user.getId(), startDate, endDate)
                .stream()
                .map(ExpenseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ExpenseDto> getExpensesByCategory(String username, Long categoryId) {
        User user = userService.findByUsername(username);
        return expenseRepository.findByUserIdAndCategoryId(user.getId(), categoryId)
                .stream()
                .map(ExpenseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public ExpenseDto getExpenseById(Long id, String username) {
        User user = userService.findByUsername(username);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to expense");
        }

        return ExpenseDto.fromEntity(expense);
    }

    @Transactional
    public ExpenseDto updateExpense(Long id, ExpenseDto dto, String username) {
        log.info("Updating expense: {} for user: {}", id, username);

        User user = userService.findByUsername(username);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to expense");
        }

        Category category = categoryService.getCategoryById(dto.getCategoryId());

        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setCategory(category);
        expense.setPaymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod()));
        expense.setNotes(dto.getNotes());

        Expense updatedExpense = expenseRepository.save(expense);
        log.info("Expense updated: {}", updatedExpense.getId());

        return ExpenseDto.fromEntity(updatedExpense);
    }

    @Transactional
    public void deleteExpense(Long id, String username) {
        log.info("Deleting expense: {} for user: {}", id, username);

        User user = userService.findByUsername(username);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to expense");
        }

        expenseRepository.delete(expense);
        log.info("Expense deleted: {}", id);
    }

    public BigDecimal getTotalExpenses(String username) {
        User user = userService.findByUsername(username);
        BigDecimal total = expenseRepository.getTotalExpenses(user.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpensesBetweenDates(String username, LocalDate startDate, LocalDate endDate) {
        User user = userService.findByUsername(username);
        BigDecimal total = expenseRepository.getTotalExpensesBetweenDates(user.getId(), startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyTotal(String username, int year, int month) {
        User user = userService.findByUsername(username);
        BigDecimal total = expenseRepository.getMonthlyTotal(user.getId(), year, month);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getCurrentMonthTotal(String username) {
        YearMonth now = YearMonth.now();
        return getMonthlyTotal(username, now.getYear(), now.getMonthValue());
    }

    public List<ExpenseDto> getRecentExpenses(String username, int limit) {
        User user = userService.findByUsername(username);
        Pageable pageable = PageRequest.of(0, limit);
        return expenseRepository.findRecentExpenses(user.getId(), pageable)
                .stream()
                .map(ExpenseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public User findByUsername(String username) {
        return userService.findByUsername(username);
    }
}