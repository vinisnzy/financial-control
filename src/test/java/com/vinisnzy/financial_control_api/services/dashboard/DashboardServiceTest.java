package com.vinisnzy.financial_control_api.services.dashboard;

import com.vinisnzy.financial_control_api.dtos.dashboard.CategoryShare;
import com.vinisnzy.financial_control_api.dtos.dashboard.CategoryTotal;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthTotal;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseRepository;
import com.vinisnzy.financial_control_api.repositories.income.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    ExpenseRepository expenseRepository;
    @Mock
    IncomeRepository incomeRepository;

    @InjectMocks
    DashboardService service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getMonthlySummary_shouldCalculatePlannedAndRealizedBalance() {
        var month = YearMonth.of(2026, 3);
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        when(incomeRepository.sumPlanned(userId, start, end)).thenReturn(amount("5000.00"));
        when(incomeRepository.sumReceived(userId, start, end)).thenReturn(amount("3000.00"));
        when(expenseRepository.sumPlanned(userId, start, end)).thenReturn(amount("4200.00"));
        when(expenseRepository.sumPaid(userId, start, end)).thenReturn(amount("1000.00"));

        var response = service.getMonthlySummary(month, userId);

        assertThat(response.month()).isEqualTo(month);
        assertThat(response.plannedIncome()).isEqualByComparingTo("5000.00");
        assertThat(response.receivedIncome()).isEqualByComparingTo("3000.00");
        assertThat(response.plannedExpense()).isEqualByComparingTo("4200.00");
        assertThat(response.paidExpense()).isEqualByComparingTo("1000.00");
        assertThat(response.plannedBalance()).isEqualByComparingTo("800.00");
        assertThat(response.realizedBalance()).isEqualByComparingTo("2000.00");
    }

    @Test
    void getMonthlySummary_shouldUseFirstAndLastDayOfTheMonth() {
        var month = YearMonth.of(2026, 2);
        var start = LocalDate.of(2026, 2, 1);
        var end = LocalDate.of(2026, 2, 28);
        when(incomeRepository.sumPlanned(userId, start, end)).thenReturn(BigDecimal.ZERO);
        when(incomeRepository.sumReceived(userId, start, end)).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumPlanned(userId, start, end)).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumPaid(userId, start, end)).thenReturn(BigDecimal.ZERO);

        service.getMonthlySummary(month, userId);

        verify(incomeRepository).sumPlanned(userId, start, end);
        verify(expenseRepository).sumPaid(userId, start, end);
    }

    @Test
    void getExpensesByCategory_shouldCalculatePercentageOfEachCategory() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        var rent = new CategoryTotal(UUID.randomUUID(), "Rent", amount("750.00"));
        var food = new CategoryTotal(UUID.randomUUID(), "Food", amount("250.00"));
        when(expenseRepository.sumByCategory(userId, start, end)).thenReturn(List.of(rent, food));

        var response = service.getExpensesByCategory(start, end, userId);

        assertThat(response).extracting(CategoryShare::categoryName).containsExactly("Rent", "Food");
        assertThat(response.get(0).percentage()).isEqualByComparingTo("75.00");
        assertThat(response.get(1).percentage()).isEqualByComparingTo("25.00");
        assertThat(response.get(0).total()).isEqualByComparingTo("750.00");
    }

    @Test
    void getExpensesByCategory_percentagesShouldRoundToTwoDecimalPlaces() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        var a = new CategoryTotal(UUID.randomUUID(), "A", amount("100.00"));
        var b = new CategoryTotal(UUID.randomUUID(), "B", amount("200.00"));
        when(expenseRepository.sumByCategory(userId, start, end)).thenReturn(List.of(a, b));

        var response = service.getExpensesByCategory(start, end, userId);

        assertThat(response.get(0).percentage()).isEqualByComparingTo("33.33");
        assertThat(response.get(1).percentage()).isEqualByComparingTo("66.67");
    }

    @Test
    void getExpensesByCategory_whenGrandTotalIsZero_shouldReturnZeroPercentage() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        var free = new CategoryTotal(UUID.randomUUID(), "Free", BigDecimal.ZERO);
        when(expenseRepository.sumByCategory(userId, start, end)).thenReturn(List.of(free));

        var response = service.getExpensesByCategory(start, end, userId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().percentage()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getExpensesByCategory_whenNoRecords_shouldReturnEmptyList() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        when(expenseRepository.sumByCategory(userId, start, end)).thenReturn(List.of());

        assertThat(service.getExpensesByCategory(start, end, userId)).isEmpty();
    }

    @Test
    void getIncomesByCategory_shouldCalculatePercentageOfEachCategory() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);
        var salary = new CategoryTotal(UUID.randomUUID(), "Salary", amount("900.00"));
        var gifts = new CategoryTotal(UUID.randomUUID(), "Gifts", amount("100.00"));
        when(incomeRepository.sumByCategory(userId, start, end)).thenReturn(List.of(salary, gifts));

        var response = service.getIncomesByCategory(start, end, userId);

        assertThat(response.get(0).percentage()).isEqualByComparingTo("90.00");
        assertThat(response.get(1).percentage()).isEqualByComparingTo("10.00");
    }

    @Test
    void getExpensesByCategory_whenPeriodIsMissing_shouldThrowAndNotQuery() {
        assertThatThrownBy(() -> service.getExpensesByCategory(null, null, userId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.INVALID_PERIOD));

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void getIncomesByCategory_whenStartIsAfterEnd_shouldThrowAndNotQuery() {
        var start = LocalDate.of(2026, 3, 31);
        var end = LocalDate.of(2026, 3, 1);

        assertThatThrownBy(() -> service.getIncomesByCategory(start, end, userId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.INVALID_PERIOD));

        verifyNoInteractions(incomeRepository);
    }

    @Test
    void getMonthlyCashFlow_shouldFillMonthsWithoutRecordsWithZeroInChronologicalOrder() {
        var current = YearMonth.now();
        var first = current.minusMonths(2);
        var start = first.atDay(1);
        var end = current.atEndOfMonth();
        when(incomeRepository.monthlyTotals(userId, start, end)).thenReturn(List.of(
                monthTotal(first, "1000.00"),
                monthTotal(current, "500.00")));
        when(expenseRepository.monthlyTotals(userId, start, end)).thenReturn(List.of(
                monthTotal(first, "400.00")));

        var response = service.getMonthlyCashFlow(3, userId);

        assertThat(response).hasSize(3);
        assertThat(response).extracting(r -> r.month())
                .containsExactly(first, first.plusMonths(1), current);

        // month with income and expense
        assertThat(response.get(0).income()).isEqualByComparingTo("1000.00");
        assertThat(response.get(0).expense()).isEqualByComparingTo("400.00");
        assertThat(response.get(0).balance()).isEqualByComparingTo("600.00");
        // month without any record
        assertThat(response.get(1).income()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.get(1).expense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.get(1).balance()).isEqualByComparingTo(BigDecimal.ZERO);
        // month with income only: balance equals the income
        assertThat(response.get(2).balance()).isEqualByComparingTo("500.00");
    }

    @Test
    void getMonthlyCashFlow_shouldReturnNegativeBalanceWhenExpenseExceedsIncome() {
        var current = YearMonth.now();
        var start = current.atDay(1);
        var end = current.atEndOfMonth();
        when(incomeRepository.monthlyTotals(userId, start, end)).thenReturn(List.of(monthTotal(current, "100.00")));
        when(expenseRepository.monthlyTotals(userId, start, end)).thenReturn(List.of(monthTotal(current, "350.00")));

        var response = service.getMonthlyCashFlow(1, userId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().balance()).isEqualByComparingTo("-250.00");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 25})
    void getMonthlyCashFlow_whenMonthsIsOutOfRange_shouldThrowAndNotQuery(int months) {
        assertThatThrownBy(() -> service.getMonthlyCashFlow(months, userId))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCode.INVALID_PERIOD));

        verifyNoInteractions(incomeRepository, expenseRepository);
    }

    @Test
    void getInstallmentsProjection_shouldStartNextMonthAndFillEmptyMonthsWithZero() {
        var next = YearMonth.now().plusMonths(1);
        when(expenseRepository.projectInstallments(userId, LocalDate.now())).thenReturn(List.of(
                monthTotal(next, "300.00"),
                monthTotal(next.plusMonths(2), "150.00")));

        var response = service.getInstallmentsProjection(3, userId);

        assertThat(response).extracting(r -> r.month())
                .containsExactly(next, next.plusMonths(1), next.plusMonths(2));
        assertThat(response.get(0).committed()).isEqualByComparingTo("300.00");
        assertThat(response.get(1).committed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.get(2).committed()).isEqualByComparingTo("150.00");
    }

    @Test
    void getInstallmentsProjection_whenNoInstallments_shouldReturnAllMonthsWithZero() {
        when(expenseRepository.projectInstallments(userId, LocalDate.now())).thenReturn(List.of());

        var response = service.getInstallmentsProjection(2, userId);

        assertThat(response).hasSize(2);
        assertThat(response).allSatisfy(r -> assertThat(r.committed()).isEqualByComparingTo(BigDecimal.ZERO));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 25})
    void getInstallmentsProjection_whenMonthsIsOutOfRange_shouldThrowAndNotQuery(int months) {
        assertThatThrownBy(() -> service.getInstallmentsProjection(months, userId))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(expenseRepository);
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private static MonthTotal monthTotal(YearMonth month, String total) {
        return new MonthTotal(month.getYear(), month.getMonthValue(), amount(total));
    }
}
