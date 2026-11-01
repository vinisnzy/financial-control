package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FixedExpenseRepository extends JpaRepository<FixedExpense, UUID> {
    Optional<FixedExpense> findByIdAndUserId(UUID id, UUID userId);

    Page<FixedExpense> findByUserId(UUID userId, Pageable pageable);

    Page<FixedExpense> findByExpenseCategoryIdAndUserId(UUID expenseCategoryId, UUID userId, Pageable pageable);
}
