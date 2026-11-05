package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.entities.expense.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
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
}
