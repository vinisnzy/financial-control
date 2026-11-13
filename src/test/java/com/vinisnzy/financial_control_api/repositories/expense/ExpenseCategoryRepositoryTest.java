package com.vinisnzy.financial_control_api.repositories.expense;

import com.vinisnzy.financial_control_api.TestcontainersConfig;
import com.vinisnzy.financial_control_api.config.JpaConfig;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.repositories.utils.PersistUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, JpaConfig.class, PersistUtil.class})
class ExpenseCategoryRepositoryTest {

    @Autowired
    ExpenseCategoryRepository repository;
    @Autowired
    PersistUtil persistUtil;

    @Test
    void findByIdAndUserIdOrDefault_shouldReturnOwnedOrDefault_butNotOthers() {
        var owner = persistUtil.persistUser("owner@test.com");
        var other = persistUtil.persistUser("other@test.com");

        var own = persistUtil.persistExpenseCategory("own", owner, false);
        var foreign = persistUtil.persistExpenseCategory("foreign", other, false);
        var standard = persistUtil.persistExpenseCategory("default", null, true);

        assertThat(repository.findByIdAndUserIdOrDefault(own.getId(), owner.getId())).isPresent();
        assertThat(repository.findByIdAndUserIdOrDefault(standard.getId(), owner.getId())).isPresent();
        assertThat(repository.findByIdAndUserIdOrDefault(foreign.getId(), owner.getId())).isNotPresent();
    }

    @Test
    void findByIdAndUserIdOrDefault_whenUserDoesNotExist_shouldReturnOnlyDefault() {
        var owner = persistUtil.persistUser("owner@test.com");
        var own = persistUtil.persistExpenseCategory("own", owner, false);
        var standard = persistUtil.persistExpenseCategory("default", null, true);
        var unknownUserId = UUID.randomUUID();

        assertThat(repository.findByIdAndUserIdOrDefault(own.getId(), unknownUserId)).isEmpty();
        assertThat(repository.findByIdAndUserIdOrDefault(standard.getId(), unknownUserId)).isPresent();
    }

    @Test
    void findByUserIdOrDefault_shouldReturnOwnedAndDefault_butNotOthers() {
        var owner = persistUtil.persistUser("owner@test.com");
        var other = persistUtil.persistUser("other@test.com");
        persistUtil.persistExpenseCategory("own", owner, false);
        persistUtil.persistExpenseCategory("default", null, true);
        persistUtil.persistExpenseCategory("foreign", other, false);

        var response = repository.findByUserIdOrDefault(owner.getId(), PageRequest.of(0, 10));

        assertThat(response.getContent()).extracting(ExpenseCategory::getName)
                .containsExactlyInAnyOrder("own", "default");
    }

    @Test
    void findByUserIdOrDefault_whenUserHasNoCategories_shouldReturnOnlyDefaults() {
        var owner = persistUtil.persistUser("owner@test.com");
        var other = persistUtil.persistUser("other@test.com");
        persistUtil.persistExpenseCategory("default", null, true);
        persistUtil.persistExpenseCategory("foreign", other, false);

        var response = repository.findByUserIdOrDefault(owner.getId(), PageRequest.of(0, 10));

        assertThat(response.getContent()).extracting(ExpenseCategory::getName).containsExactly("default");
    }
}
