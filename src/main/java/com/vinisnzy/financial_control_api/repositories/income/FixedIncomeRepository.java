package com.vinisnzy.financial_control_api.repositories.income;

import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FixedIncomeRepository extends JpaRepository<FixedIncome, UUID> {
    Optional<FixedIncome> findByIdAndUserId(UUID id, UUID userId);

    Page<FixedIncome> findByUserId(UUID userId, Pageable pageable);

    @Query("""
            SELECT f FROM FixedIncome f
            WHERE f.startDate <= :end
            AND (f.endDate IS NULL OR f.endDate >= :start)
            """)
    List<FixedIncome> findActiveBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
