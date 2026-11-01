package com.vinisnzy.financial_control_api.repositories.income;

import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FixedIncomeRepository extends JpaRepository<FixedIncome, UUID> {
    Optional<FixedIncome> findByIdAndUserId(UUID id, UUID userId);

    Page<FixedIncome> findByUserId(UUID userId, Pageable pageable);
}
