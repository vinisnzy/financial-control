package com.vinisnzy.financial_control_api.repositories.income;

import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, UUID> {
    @Query("""
            SELECT c FROM IncomeCategory c
            WHERE c.id = :id AND (c.user.id = :userId OR c.isDefault = true)
            """)
    Optional<IncomeCategory> findByIdAndUserIdOrDefault(UUID id, UUID userId);

    @Query("""
            SELECT c FROM IncomeCategory c
            WHERE c.user.id = :userId OR c.isDefault = true
            """)
    Page<IncomeCategory> findByUserIdOrDefault(UUID userId, Pageable pageable);
}
