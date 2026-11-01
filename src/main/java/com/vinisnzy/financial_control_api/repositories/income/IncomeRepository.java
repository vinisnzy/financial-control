package com.vinisnzy.financial_control_api.repositories.income;

import com.vinisnzy.financial_control_api.entities.income.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID> {
    Optional<Income> findByIdAndUserId(UUID id, UUID userId);

    Page<Income> findByNameContainingIgnoreCaseAndUserId(String name, UUID userId, Pageable pageable);

    Page<Income> findByUserId(UUID userId, Pageable pageable);

    Page<Income> findByBankAccountIdAndUserId(UUID bankAccountId, UUID userId, Pageable pageable);

    Page<Income> findByIncomeCategoryIdAndUserId(UUID incomeCategoryId, UUID userId, Pageable pageable);

    Page<Income> findByUserIdAndExpectedAtBetween(UUID userId, LocalDate start, LocalDate end, Pageable pageable);

    Page<Income> findByBankAccountIdAndUserIdAndExpectedAtBetween(
            UUID bankAccountId, UUID userId, LocalDate start, LocalDate end, Pageable pageable);

    Page<Income> findByIncomeCategoryIdAndUserIdAndExpectedAtBetween(
            UUID incomeCategoryId, UUID userId, LocalDate start, LocalDate end, Pageable pageable);
}
