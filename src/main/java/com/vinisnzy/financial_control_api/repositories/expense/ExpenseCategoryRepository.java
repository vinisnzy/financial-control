package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {
    @Query("""
            SELECT c FROM ExpenseCategory c
            WHERE c.id = :id AND (c.user.id = :userId OR c.isDefault = true)
            """)
    Optional<ExpenseCategory> findByIdAndUserIdOrDefault(UUID id, UUID userId);

    @Query("""
            SELECT c FROM ExpenseCategory c
            WHERE c.user.id = :userId OR c.isDefault = true
            """)
    Page<ExpenseCategory> findByUserIdOrDefault(UUID userId, Pageable pageable);
}
