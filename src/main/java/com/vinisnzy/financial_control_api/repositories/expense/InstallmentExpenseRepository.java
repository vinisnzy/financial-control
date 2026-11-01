package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.entities.expense.InstallmentExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstallmentExpenseRepository extends JpaRepository<InstallmentExpense, UUID> {
    Optional<InstallmentExpense> findByIdAndUserId(UUID id, UUID userId);

    Page<InstallmentExpense> findByUserId(UUID userId, Pageable pageable);
}
