package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.TestcontainersConfig;
import com.vinisnzy.financial_control_api.config.JpaConfig;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.repositories.utils.PersistUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class, PersistUtil.class})
class FixedExpenseRepositoryTest {

    @Autowired
    private FixedExpenseRepository repository;

    @Autowired
    private PersistUtil persistUtil;

    @Test
    void findActiveBetween_shouldReturnOnlyOverlappingFixedExpenses() {
        var user = persistUtil.persistUser("vini@test.com");
        persistUtil.persistFixedExpense(user, "inside", date(2026, 1, 1), null);
        persistUtil.persistFixedExpense(user, "ends", date(2026, 1, 1), date(2026, 3, 1));
        persistUtil.persistFixedExpense(user, "starts", date(2026, 3, 31), null);
        persistUtil.persistFixedExpense(user, "ended", date(2025, 1, 1), date(2026, 2, 28));
        persistUtil.persistFixedExpense(user, "future", date(2026, 4, 1), null);

        var response = repository.findActiveBetween(date(2026, 3, 1), date(2026, 3, 31));

        assertThat(response).extracting(FixedExpense::getName)
                .containsExactlyInAnyOrder("inside", "ends", "starts");
    }

    @Test
    void findActiveBetween_whenNoneActive_shouldReturnEmpty() {
        var user = persistUtil.persistUser("vini@test.com");
        persistUtil.persistFixedExpense(user, "ended", date(2025, 1, 1), date(2026, 2, 28));
        persistUtil.persistFixedExpense(user, "future", date(2026, 4, 1), null);

        var response = repository.findActiveBetween(date(2026, 3, 1), date(2026, 3, 31));

        assertThat(response).isEmpty();
    }

    @Test
    void findActiveBetween_withSingleDayRange_shouldReturnOnlyThoseActiveOnThatDay() {
        var user = persistUtil.persistUser("vini@test.com");
        persistUtil.persistFixedExpense(user, "active", date(2026, 1, 1), null);
        persistUtil.persistFixedExpense(user, "startsThatDay", date(2026, 3, 15), null);
        persistUtil.persistFixedExpense(user, "endsThatDay", date(2026, 1, 1), date(2026, 3, 15));
        persistUtil.persistFixedExpense(user, "startsNextDay", date(2026, 3, 16), null);
        persistUtil.persistFixedExpense(user, "endedDayBefore", date(2026, 1, 1), date(2026, 3, 14));

        var response = repository.findActiveBetween(date(2026, 3, 15), date(2026, 3, 15));

        assertThat(response).extracting(FixedExpense::getName)
                .containsExactlyInAnyOrder("active", "startsThatDay", "endsThatDay");
    }

    private LocalDate date(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
}
