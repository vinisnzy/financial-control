package com.vinisnzy.financial_control_api.services.dashboard;

import com.vinisnzy.financial_control_api.dtos.dashboard.*;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseRepository;
import com.vinisnzy.financial_control_api.repositories.income.IncomeRepository;
import com.vinisnzy.financial_control_api.services.common.PeriodValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DashboardService {
    private static final int MAX_MONTHS = 24;

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public DashboardService(ExpenseRepository expenseRepository, IncomeRepository incomeRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    public MonthlySummaryResponse getMonthlySummary(YearMonth month, UUID userId) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        BigDecimal plannedIncome = incomeRepository.sumPlanned(userId, start, end);
        BigDecimal receivedIncome = incomeRepository.sumReceived(userId, start, end);
        BigDecimal plannedExpense = expenseRepository.sumPlanned(userId, start, end);
        BigDecimal paidExpense = expenseRepository.sumPaid(userId, start, end);

        return new MonthlySummaryResponse(
                month,
                plannedIncome,
                receivedIncome,
                plannedExpense,
                paidExpense,
                plannedIncome.subtract(plannedExpense),
                receivedIncome.subtract(paidExpense)
        );
    }

    public List<CategoryShare> getExpensesByCategory(LocalDate start, LocalDate end, UUID userId) {
        requirePeriod(start, end);
        return withPercentage(expenseRepository.sumByCategory(userId, start, end));
    }

    public List<CategoryShare> getIncomesByCategory(LocalDate start, LocalDate end, UUID userId) {
        requirePeriod(start, end);
        return withPercentage(incomeRepository.sumByCategory(userId, start, end));
    }

    public List<MonthComparison> getMonthlyCashFlow(int months, UUID userId) {
        validateMonths(months);
        YearMonth last = YearMonth.now();
        YearMonth first = last.minusMonths(months - 1L);

        Map<YearMonth, BigDecimal> incomes = toMap(
                incomeRepository.monthlyTotals(userId, first.atDay(1), last.atEndOfMonth()));
        Map<YearMonth, BigDecimal> expenses = toMap(
                expenseRepository.monthlyTotals(userId, first.atDay(1), last.atEndOfMonth()));

        return Stream.iterate(first, m -> m.plusMonths(1))
                .limit(months)
                .map(m -> {
                    BigDecimal income = incomes.getOrDefault(m, BigDecimal.ZERO);
                    BigDecimal expense = expenses.getOrDefault(m, BigDecimal.ZERO);
                    return new MonthComparison(m, income, expense, income.subtract(expense));
                }).toList();
    }

    public List<InstallmentProjection> getInstallmentsProjection(int months, UUID userId) {
        validateMonths(months);
        YearMonth first = YearMonth.now().plusMonths(1);
        Map<YearMonth, BigDecimal> committed = toMap(
                expenseRepository.projectInstallments(userId, LocalDate.now()));

        return Stream.iterate(first, m -> m.plusMonths(1))
                .limit(months)
                .map(m -> new InstallmentProjection(m, committed.getOrDefault(m, BigDecimal.ZERO)))
                .toList();
    }

    private List<CategoryShare> withPercentage(List<CategoryTotal> totals) {
        BigDecimal grand = totals.stream()
                .map(CategoryTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totals.stream()
                .map(t -> new CategoryShare(
                        t.categoryId(),
                        t.categoryName(),
                        t.total(),
                        grand.signum() == 0 ? BigDecimal.ZERO : t.total()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(grand, 2, RoundingMode.HALF_UP)))
                .toList();
    }

    private Map<YearMonth, BigDecimal> toMap(List<MonthTotal> totals) {
        return totals.stream().collect(Collectors.toMap(
                t -> YearMonth.of(t.year(), t.month()), MonthTotal::total));
    }

    private void requirePeriod(LocalDate start, LocalDate end) {
        if (!PeriodValidator.hasPeriod(start, end)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD, "'start' and 'end' are required.");
        }
    }

    private void validateMonths(int months) {
        if (months < 1 || months > MAX_MONTHS) {
            throw new BusinessException(
                    ErrorCode.INVALID_PERIOD,
                    "'months' must be between 1 and %d.".formatted(MAX_MONTHS)
            );
        }
    }
}
