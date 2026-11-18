package com.vinisnzy.financial_control_api.repositories.income;

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
class IncomeRepositoryTest {

    private static final LocalDate START = LocalDate.of(2026, 3, 1);
    private static final LocalDate END = LocalDate.of(2026, 3, 31);

    @Autowired
    private IncomeRepository repository;

    @Autowired
    private PersistUtil persistUtil;

    @Test
    void sumPlanned_shouldSumOnlyExpectedAtInsideThePeriodInclusive() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistIncomeCategory("Salary", user, false);
        var account = persistUtil.persistBankAccount("Main", user);
        persistUtil.persistIncome(user, category, account, amount("100.00"), START, null);
        persistUtil.persistIncome(user, category, account, amount("50.50"), END, null);
        persistUtil.persistIncome(user, category, account, amount("999.00"), START.minusDays(1), null);
        persistUtil.persistIncome(user, category, account, amount("999.00"), END.plusDays(1), null);

        var total = repository.sumPlanned(user.getId(), START, END);

        assertThat(total).isEqualByComparingTo("150.50");
    }

    @Test
    void sumPlanned_shouldIgnoreOtherUsers() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistIncomeCategory("Salary", user, false);
        var otherCategory = persistUtil.persistIncomeCategory("Salary", other, false);
        var account = persistUtil.persistBankAccount("Main", user);
        var otherAccount = persistUtil.persistBankAccount("Main", other);
        persistUtil.persistIncome(user, category, account, amount("100.00"), date(10), null);
        persistUtil.persistIncome(other, otherCategory, otherAccount, amount("700.00"), date(10), null);

        assertThat(repository.sumPlanned(user.getId(), START, END)).isEqualByComparingTo("100.00");
        assertThat(repository.sumPlanned(other.getId(), START, END)).isEqualByComparingTo("700.00");
    }

    @Test
    void sumPlanned_whenNoRecords_shouldReturnZero() {
        var user = persistUtil.persistUser("vini@test.com");

        assertThat(repository.sumPlanned(user.getId(), START, END)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumReceived_shouldSumOnlyReceivedAtInsideThePeriod() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistIncomeCategory("Salary", user, false);
        var account = persistUtil.persistBankAccount("Main", user);
        persistUtil.persistIncome(user, category, account, amount("100.00"), date(5), date(6));
        persistUtil.persistIncome(user, category, account, amount("40.00"), date(5), END);
        // expected in range but not received: must not count
        persistUtil.persistIncome(user, category, account, amount("500.00"), date(5), null);
        // received outside the period: must not count
        persistUtil.persistIncome(user, category, account, amount("500.00"), date(5), START.minusDays(1));
        persistUtil.persistIncome(user, category, account, amount("500.00"), date(5), END.plusDays(1));

        var total = repository.sumReceived(user.getId(), START, END);

        assertThat(total).isEqualByComparingTo("140.00");
    }

    @Test
    void sumReceived_shouldIgnoreOtherUsers() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistIncomeCategory("Salary", other, false);
        var account = persistUtil.persistBankAccount("Main", other);
        persistUtil.persistIncome(other, category, account, amount("300.00"), date(5), date(6));

        assertThat(repository.sumReceived(user.getId(), START, END)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumReceived_whenNoRecords_shouldReturnZero() {
        var user = persistUtil.persistUser("vini@test.com");

        assertThat(repository.sumReceived(user.getId(), START, END)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumByCategory_shouldGroupByCategoryOrderedByTotalDescending() {
        var user = persistUtil.persistUser("vini@test.com");
        var salary = persistUtil.persistIncomeCategory("Salary", user, false);
        var freelance = persistUtil.persistIncomeCategory("Freelance", user, false);
        var gifts = persistUtil.persistIncomeCategory("Gifts", user, false);
        var account = persistUtil.persistBankAccount("Main", user);
        persistUtil.persistIncome(user, gifts, account, amount("30.00"), date(2), null);
        persistUtil.persistIncome(user, gifts, account, amount("20.00"), date(9), null);
        persistUtil.persistIncome(user, salary, account, amount("5000.00"), date(5), null);
        persistUtil.persistIncome(user, freelance, account, amount("800.00"), date(20), null);

        var result = repository.sumByCategory(user.getId(), START, END);

        assertThat(result).extracting(CategoryTotal::categoryName)
                .containsExactly("Salary", "Freelance", "Gifts");
        assertThat(result).extracting(CategoryTotal::total)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(amount("5000.00"), amount("800.00"), amount("50.00"));
        assertThat(result.getFirst().categoryId()).isEqualTo(salary.getId());
    }

    @Test
    void sumByCategory_shouldIgnoreIncomesOutsideThePeriodAndOtherUsers() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistIncomeCategory("Salary", user, false);
        var otherCategory = persistUtil.persistIncomeCategory("Secret", other, false);
        var account = persistUtil.persistBankAccount("Main", user);
        var otherAccount = persistUtil.persistBankAccount("Main", other);
        persistUtil.persistIncome(user, category, account, amount("10.00"), date(10), null);
        persistUtil.persistIncome(user, category, account, amount("999.00"), END.plusDays(1), null);
        persistUtil.persistIncome(other, otherCategory, otherAccount, amount("999.00"), date(10), null);

        var result = repository.sumByCategory(user.getId(), START, END);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().categoryName()).isEqualTo("Salary");
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
        var category = persistUtil.persistIncomeCategory("Salary", user, false);
        var account = persistUtil.persistBankAccount("Main", user);
        persistUtil.persistIncome(user, category, account, amount("10.00"), LocalDate.of(2026, 3, 31), null);
        persistUtil.persistIncome(user, category, account, amount("20.00"), LocalDate.of(2026, 1, 1), null);
        persistUtil.persistIncome(user, category, account, amount("30.00"), LocalDate.of(2026, 1, 15), null);
        // crosses the year boundary to prove ordering is by (year, month), not month alone
        persistUtil.persistIncome(user, category, account, amount("5.00"), LocalDate.of(2025, 12, 20), null);

        var result = repository.monthlyTotals(user.getId(), LocalDate.of(2025, 12, 1), END);

        assertThat(result).extracting(MonthTotal::year, MonthTotal::month)
                .containsExactly(tuple(2025, 12), tuple(2026, 1), tuple(2026, 3));
        assertThat(result).extracting(MonthTotal::total)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(amount("5.00"), amount("50.00"), amount("10.00"));
    }

    @Test
    void monthlyTotals_shouldOmitMonthsWithoutRecords() {
        var user = persistUtil.persistUser("vini@test.com");
        var category = persistUtil.persistIncomeCategory("Salary", user, false);
        var account = persistUtil.persistBankAccount("Main", user);
        persistUtil.persistIncome(user, category, account, amount("10.00"), LocalDate.of(2026, 1, 10), null);
        persistUtil.persistIncome(user, category, account, amount("10.00"), LocalDate.of(2026, 3, 10), null);

        var result = repository.monthlyTotals(user.getId(), LocalDate.of(2026, 1, 1), END);

        assertThat(result).extracting(MonthTotal::month).containsExactly(1, 3);
    }

    @Test
    void monthlyTotals_shouldIgnoreOtherUsersAndOutOfRange() {
        var user = persistUtil.persistUser("vini@test.com");
        var other = persistUtil.persistUser("other@test.com");
        var category = persistUtil.persistIncomeCategory("Salary", user, false);
        var otherCategory = persistUtil.persistIncomeCategory("Salary", other, false);
        var account = persistUtil.persistBankAccount("Main", user);
        var otherAccount = persistUtil.persistBankAccount("Main", other);
        persistUtil.persistIncome(user, category, account, amount("10.00"), date(10), null);
        persistUtil.persistIncome(user, category, account, amount("99.00"), END.plusDays(1), null);
        persistUtil.persistIncome(other, otherCategory, otherAccount, amount("99.00"), date(10), null);

        var result = repository.monthlyTotals(user.getId(), START, END);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().total()).isEqualByComparingTo("10.00");
    }

    @Test
    void monthlyTotals_whenNoRecords_shouldReturnEmptyList() {
        var user = persistUtil.persistUser("vini@test.com");

        assertThat(repository.monthlyTotals(user.getId(), START, END)).isEmpty();
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private static LocalDate date(int day) {
        return LocalDate.of(2026, 3, day);
    }
}
