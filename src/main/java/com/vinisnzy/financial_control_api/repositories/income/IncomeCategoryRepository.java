package com.vinisnzy.financial_control_api.repositories.income;

import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, UUID> {
    Optional<IncomeCategory> findByIdAndUserId(UUID id, UUID userId);

    Page<IncomeCategory> findByUserId(UUID userId, Pageable pageable);
}
