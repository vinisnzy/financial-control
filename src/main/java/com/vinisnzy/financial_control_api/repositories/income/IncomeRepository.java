package com.vinisnzy.financial_control_api.repositories.income;

import com.vinisnzy.financial_control_api.dtos.dashboard.CategoryTotal;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthTotal;
import com.vinisnzy.financial_control_api.entities.income.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    boolean existsByFixedIncomeIdAndExpectedAtBetween(UUID fixedIncomeId, LocalDate start, LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(i.amount), 0)
            FROM Income i
            WHERE i.user.id = :userId
              AND i.expectedAt BETWEEN :start AND :end
            """)
    BigDecimal sumPlanned(UUID userId, LocalDate start, LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(i.amount), 0)
            FROM Income i
            WHERE i.user.id = :userId
              AND i.receivedAt BETWEEN :start AND :end
            """)
    BigDecimal sumReceived(UUID userId, LocalDate start, LocalDate end);

    @Query("""
            SELECT new com.vinisnzy.financial_control_api.dtos.dashboard.CategoryTotal(
                c.id, c.name, SUM(i.amount))
            FROM Income i
            JOIN i.incomeCategory c
            WHERE i.user.id = :userId
              AND i.expectedAt BETWEEN :start AND :end
            GROUP BY c.id, c.name
            ORDER BY SUM(i.amount) DESC
            """)
    List<CategoryTotal> sumByCategory(UUID userId, LocalDate start, LocalDate end);

    @Query("""
            SELECT new com.vinisnzy.financial_control_api.dtos.dashboard.MonthTotal(
                YEAR(i.expectedAt), MONTH(i.expectedAt), SUM(i.amount))
            FROM Income i
            WHERE i.user.id = :userId
              AND i.expectedAt BETWEEN :start AND :end
            GROUP BY YEAR(i.expectedAt), MONTH(i.expectedAt)
            ORDER BY YEAR(i.expectedAt), MONTH(i.expectedAt)
            """)
    List<MonthTotal> monthlyTotals(UUID userId, LocalDate start, LocalDate end);
}
