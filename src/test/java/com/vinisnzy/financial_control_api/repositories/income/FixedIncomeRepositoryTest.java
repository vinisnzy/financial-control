package com.vinisnzy.financial_control_api.repositories.income;

import com.vinisnzy.financial_control_api.TestcontainersConfig;
import com.vinisnzy.financial_control_api.config.JpaConfig;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.utils.PersistUtil;
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
class FixedIncomeRepositoryTest {

    @Autowired
    private FixedIncomeRepository repository;

    @Autowired
    private PersistUtil persistUtil;

    @Test
    void findActiveBetween_shouldReturnOnlyOverlappingFixedIncomes() {
        var user = persistUtil.persistUser("vini@test.com");
        persistUtil.persistFixedIncome(user, "inside", date(2026, 1, 1), null);
        persistUtil.persistFixedIncome(user, "ends", date(2026, 1, 1), date(2026, 3, 1));
        persistUtil.persistFixedIncome(user, "starts", date(2026, 3, 31), null);
        persistUtil.persistFixedIncome(user, "spans", date(2026, 1, 1), date(2026, 12, 31));
        persistUtil.persistFixedIncome(user, "ended", date(2025, 1, 1), date(2026, 2, 28));
        persistUtil.persistFixedIncome(user, "future", date(2026, 4, 1), null);

        var response = repository.findActiveBetween(date(2026, 3, 1), date(2026, 3, 31));

        assertThat(response).extracting(FixedIncome::getName)
                .containsExactlyInAnyOrder("inside", "ends", "starts", "spans");
    }

    @Test
    void findActiveBetween_whenNoneActive_shouldReturnEmpty() {
        var user = persistUtil.persistUser("vini@test.com");
        persistUtil.persistFixedIncome(user, "ended", date(2025, 1, 1), date(2026, 2, 28));
        persistUtil.persistFixedIncome(user, "future", date(2026, 4, 1), null);

        var response = repository.findActiveBetween(date(2026, 3, 1), date(2026, 3, 31));

        assertThat(response).isEmpty();
    }

    private LocalDate date(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
}
