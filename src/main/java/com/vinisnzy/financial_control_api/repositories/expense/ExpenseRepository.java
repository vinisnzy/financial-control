package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.dtos.dashboard.CategoryTotal;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthTotal;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    Page<Expense> findByNameContainingIgnoreCaseAndUserId(String name, UUID userId, Pageable pageable);

    Page<Expense> findByUserId(UUID userId, Pageable pageable);

    Page<Expense> findByExpenseCategoryIdAndUserId(UUID categoryId, UUID userId, Pageable pageable);

    Page<Expense> findByPaymentMethodIdAndUserId(UUID paymentMethodId, UUID userId, Pageable pageable);

    Page<Expense> findByInstallmentExpenseIdAndUserId(UUID installmentExpenseId, UUID userId, Pageable pageable);

    Page<Expense> findByUserIdAndDueDateBetween(UUID userId, LocalDate start, LocalDate end, Pageable pageable);

    Page<Expense> findByExpenseCategoryIdAndUserIdAndDueDateBetween(
            UUID categoryId, UUID userId, LocalDate start, LocalDate end, Pageable pageable);

    Page<Expense> findByPaymentMethodIdAndUserIdAndDueDateBetween(
            UUID paymentMethodId, UUID userId, LocalDate start, LocalDate end, Pageable pageable);

    boolean existsByFixedExpenseIdAndDueDateBetween(UUID fixedExpenseId, LocalDate start, LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.dueDate BETWEEN :start AND :end
            """)
    BigDecimal sumPlanned(UUID userId, LocalDate start, LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.paidAt BETWEEN :start AND :end
            """)
    BigDecimal sumPaid(UUID userId, LocalDate start, LocalDate end);

    @Query("""
            SELECT new com.vinisnzy.financial_control_api.dtos.dashboard.CategoryTotal(
                c.id, c.name, SUM(e.amount))
            FROM Expense e
            JOIN e.expenseCategory c
            WHERE e.user.id = :userId
              AND e.dueDate BETWEEN :start AND :end
            GROUP BY c.id, c.name
            ORDER BY SUM(e.amount) DESC
            """)
    List<CategoryTotal> sumByCategory(UUID userId, LocalDate start, LocalDate end);

    @Query("""
            SELECT new com.vinisnzy.financial_control_api.dtos.dashboard.MonthTotal(
                YEAR(e.dueDate), MONTH(e.dueDate), SUM(e.amount))
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.dueDate BETWEEN :start AND :end
            GROUP BY YEAR(e.dueDate), MONTH(e.dueDate)
            ORDER BY YEAR(e.dueDate), MONTH(e.dueDate)
            """)
    List<MonthTotal> monthlyTotals(UUID userId, LocalDate start, LocalDate end);

    @Query("""
            SELECT new com.vinisnzy.financial_control_api.dtos.dashboard.MonthTotal(
                YEAR(e.dueDate), MONTH(e.dueDate), SUM(e.amount))
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.installmentExpense IS NOT NULL
              AND e.paidAt IS NULL
              AND e.dueDate > :today
            GROUP BY YEAR(e.dueDate), MONTH(e.dueDate)
            ORDER BY YEAR(e.dueDate), MONTH(e.dueDate)
            """)
    List<MonthTotal> projectInstallments(UUID userId, LocalDate today);
}
