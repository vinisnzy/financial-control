package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {
    Optional<ExpenseCategory> findByIdAndUserId(UUID id, UUID userId);

    Page<ExpenseCategory> findByUserId(UUID userId, Pageable pageable);
}
