package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.TestcontainersConfig;
import com.vinisnzy.financial_control_api.config.JpaConfig;
import com.vinisnzy.financial_control_api.dtos.dashboard.CategoryTotal;
import com.vinisnzy.financial_control_api.dtos.dashboard.MonthTotal;
import com.vinisnzy.financial_control_api.repositories.utils.PersistUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@Tag("integration")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class, PersistUtil.class})
class ExpenseRepositoryTest {

    private static final LocalDate START = LocalDate.of(2026, 3, 1);
    private static final LocalDate END = LocalDate.of(2026, 3, 31);

    @Autowired
    private ExpenseRepository repository;

    @Autowired
    private PersistUtil persistUtil;

    @Test
    void sumPlanned_shouldSumOnlyDueDatesInsideThePeriodInclusive() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        persistUtil.persistExpense(user, category, payment, amount("100.00"), START, null);
        persistUtil.persistExpense(user, category, payment, amount("50.50"), END, null);
        persistUtil.persistExpense(user, category, payment, amount("999.00"), START.minusDays(1), null);
        persistUtil.persistExpense(user, category, payment, amount("999.00"), END.plusDays(1), null);

        var total = repository.sumPlanned(user.getId(), START, END);

        assertThat(total).isEqualByComparingTo("150.50");
    }

    @Test
    void sumPlanned_shouldIgnoreOtherUsers() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var otherCategory = persistUtil.persistExpenseCategory("Food", other, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        var otherPayment = persistUtil.persistPaymentMethod("Card", other);
        persistUtil.persistExpense(user, category, payment, amount("100.00"), date(10), null);
        persistUtil.persistExpense(other, otherCategory, otherPayment, amount("700.00"), date(10), null);

        assertThat(repository.sumPlanned(user.getId(), START, END)).isEqualByComparingTo("100.00");
        assertThat(repository.sumPlanned(other.getId(), START, END)).isEqualByComparingTo("700.00");
    }

    @Test
    void sumPlanned_whenNoRecords_shouldReturnZero() {
        var user = persistUtil.persistUser("vini@test.com");

        var total = repository.sumPlanned(user.getId(), START, END);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumPaid_shouldSumOnlyPaidAtInsideThePeriod() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        persistUtil.persistExpense(user, category, payment, amount("100.00"), date(5), date(6));
        persistUtil.persistExpense(user, category, payment, amount("40.00"), date(5), END);
        // due in range but not paid: must not count
        persistUtil.persistExpense(user, category, payment, amount("500.00"), date(5), null);
        // paid outside the period: must not count
        persistUtil.persistExpense(user, category, payment, amount("500.00"), date(5), START.minusDays(1));
        persistUtil.persistExpense(user, category, payment, amount("500.00"), date(5), END.plusDays(1));

        var total = repository.sumPaid(user.getId(), START, END);

        assertThat(total).isEqualByComparingTo("140.00");
    }

    @Test
    void sumPaid_shouldIgnoreOtherUsers() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistExpenseCategory("Food", other, false);
        var payment = persistUtil.persistPaymentMethod("Card", other);
        persistUtil.persistExpense(other, category, payment, amount("300.00"), date(5), date(6));

        assertThat(repository.sumPaid(user.getId(), START, END)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumPaid_whenNoRecords_shouldReturnZero() {
        var user = persistUtil.persistUser("vini@test.com");

        assertThat(repository.sumPaid(user.getId(), START, END)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumByCategory_shouldGroupByCategoryOrderedByTotalDescending() {
        var user = persistUtil.persistUser("vini@test.com");
        var food = persistUtil.persistExpenseCategory("Food", user, false);
        var rent = persistUtil.persistExpenseCategory("Rent", user, false);
        var fun = persistUtil.persistExpenseCategory("Fun", user, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        persistUtil.persistExpense(user, food, payment, amount("30.00"), date(2), null);
        persistUtil.persistExpense(user, food, payment, amount("20.00"), date(9), null);
        persistUtil.persistExpense(user, rent, payment, amount("1000.00"), date(5), null);
        persistUtil.persistExpense(user, fun, payment, amount("75.00"), date(20), null);

        var result = repository.sumByCategory(user.getId(), START, END);

        assertThat(result).extracting(CategoryTotal::categoryName)
                .containsExactly("Rent", "Fun", "Food");
        assertThat(result).extracting(CategoryTotal::total)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(amount("1000.00"), amount("75.00"), amount("50.00"));
        assertThat(result.getFirst().categoryId()).isEqualTo(rent.getId());
    }

    @Test
    void sumByCategory_shouldIgnoreExpensesOutsideThePeriodAndOtherUsers() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var otherCategory = persistUtil.persistExpenseCategory("Secret", other, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        var otherPayment = persistUtil.persistPaymentMethod("Card", other);
        persistUtil.persistExpense(user, category, payment, amount("10.00"), date(10), null);
        persistUtil.persistExpense(user, category, payment, amount("999.00"), END.plusDays(1), null);
        persistUtil.persistExpense(other, otherCategory, otherPayment, amount("999.00"), date(10), null);

        var result = repository.sumByCategory(user.getId(), START, END);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().categoryName()).isEqualTo("Food");
        assertThat(result.getFirst().total()).isEqualByComparingTo("10.00");
    }

    @Test
    void sumByCategory_whenNoRecords_shouldReturnEmptyList() {
        var user = persistUtil.persistUser("vini@test.com");

        assertThat(repository.sumByCategory(user.getId(), START, END)).isEmpty();
    }

    @Test
    void monthlyTotals_shouldGroupByMonthOrderedChronologically() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        persistUtil.persistExpense(user, category, payment, amount("10.00"), LocalDate.of(2026, 3, 31), null);
        persistUtil.persistExpense(user, category, payment, amount("20.00"), LocalDate.of(2026, 1, 1), null);
        persistUtil.persistExpense(user, category, payment, amount("30.00"), LocalDate.of(2026, 1, 15), null);
        // crosses the year boundary to prove ordering is by (year, month), not month alone
        persistUtil.persistExpense(user, category, payment, amount("5.00"), LocalDate.of(2025, 12, 20), null);

        var result = repository.monthlyTotals(user.getId(), LocalDate.of(2025, 12, 1), END);

        assertThat(result).extracting(MonthTotal::year, MonthTotal::month)
                .containsExactly(
                        tuple(2025, 12),
                        tuple(2026, 1),
                        tuple(2026, 3));
        assertThat(result).extracting(MonthTotal::total)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(amount("5.00"), amount("50.00"), amount("10.00"));
    }

    @Test
    void monthlyTotals_shouldOmitMonthsWithoutRecords() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        persistUtil.persistExpense(user, category, payment, amount("10.00"), LocalDate.of(2026, 1, 10), null);
        persistUtil.persistExpense(user, category, payment, amount("10.00"), LocalDate.of(2026, 3, 10), null);

        var result = repository.monthlyTotals(user.getId(), LocalDate.of(2026, 1, 1), END);

        assertThat(result).extracting(MonthTotal::month).containsExactly(1, 3);
    }

    @Test
    void monthlyTotals_shouldIgnoreOtherUsersAndOutOfRange() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var otherCategory = persistUtil.persistExpenseCategory("Food", other, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        var otherPayment = persistUtil.persistPaymentMethod("Card", other);
        persistUtil.persistExpense(user, category, payment, amount("10.00"), date(10), null);
        persistUtil.persistExpense(user, category, payment, amount("99.00"), END.plusDays(1), null);
        persistUtil.persistExpense(other, otherCategory, otherPayment, amount("99.00"), date(10), null);

        var result = repository.monthlyTotals(user.getId(), START, END);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().total()).isEqualByComparingTo("10.00");
    }

    @Test
    void monthlyTotals_whenNoRecords_shouldReturnEmptyList() {
        var user = persistUtil.persistUser("vini@test.com");

        assertThat(repository.monthlyTotals(user.getId(), START, END)).isEmpty();
    }

    @Test
    void projectInstallments_shouldSumOnlyFutureUnpaidInstallmentsGroupedByMonth() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistExpenseCategory("Food", user, false);
        var payment = persistUtil.persistPaymentMethod("Card", user);
        var installment = persistUtil.persistInstallmentExpense(user, category, payment, LocalDate.of(2026, 1, 10));
        var today = LocalDate.of(2026, 3, 15);

        persistUtil.persistExpense(user, category, payment, amount("100.00"), LocalDate.of(2026, 4, 10), null, installment);
        persistUtil.persistExpense(user, category, payment, amount("100.00"), LocalDate.of(2026, 4, 20), null, installment);
        persistUtil.persistExpense(user, category, payment, amount("100.00"), LocalDate.of(2026, 5, 10), null, installment);
        // already paid: must not count
        persistUtil.persistExpense(user, category, payment, amount("900.00"), LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 1), installment);
        // past due date: must not count
        persistUtil.persistExpense(user, category, payment, amount("900.00"), LocalDate.of(2026, 2, 10), null, installment);
        // due today: boundary is exclusive (dueDate > today)
        persistUtil.persistExpense(user, category, payment, amount("900.00"), today, null, installment);
        // not an installment: must not count
        persistUtil.persistExpense(user, category, payment, amount("900.00"), LocalDate.of(2026, 4, 10), null);

        var result = repository.projectInstallments(user.getId(), today);

        assertThat(result).extracting(MonthTotal::year, MonthTotal::month)
                .containsExactly(
                        tuple(2026, 4),
                        tuple(2026, 5));
        assertThat(result).extracting(MonthTotal::total)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(amount("200.00"), amount("100.00"));
    }

    @Test
    void projectInstallments_shouldIgnoreOtherUsers() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistExpenseCategory("Food", other, false);
        var payment = persistUtil.persistPaymentMethod("Card", other);
        var installment = persistUtil.persistInstallmentExpense(other, category, payment, LocalDate.of(2026, 1, 10));
        persistUtil.persistExpense(other, category, payment, amount("100.00"), LocalDate.of(2026, 4, 10), null, installment);

        var result = repository.projectInstallments(user.getId(), LocalDate.of(2026, 3, 15));

        assertThat(result).isEmpty();
    }

    @Test
    void projectInstallments_whenNoInstallments_shouldReturnEmptyList() {
        var user = persistUtil.persistUser("vini@test.com");

        assertThat(repository.projectInstallments(user.getId(), LocalDate.of(2026, 3, 15))).isEmpty();
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private static LocalDate date(int day) {
        return LocalDate.of(2026, 3, day);
    }
}
