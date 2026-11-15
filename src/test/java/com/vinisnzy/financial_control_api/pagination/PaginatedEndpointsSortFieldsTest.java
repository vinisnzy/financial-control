package com.vinisnzy.financial_control_api.pagination;

import com.vinisnzy.financial_control_api.controllers.billing.BankAccountController;
import com.vinisnzy.financial_control_api.controllers.billing.PaymentMethodController;
import com.vinisnzy.financial_control_api.controllers.expense.ExpenseCategoryController;
import com.vinisnzy.financial_control_api.controllers.expense.ExpenseController;
import com.vinisnzy.financial_control_api.controllers.expense.FixedExpenseController;
import com.vinisnzy.financial_control_api.controllers.expense.InstallmentExpenseController;
import com.vinisnzy.financial_control_api.controllers.income.FixedIncomeController;
import com.vinisnzy.financial_control_api.controllers.income.IncomeCategoryController;
import com.vinisnzy.financial_control_api.controllers.income.IncomeController;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.entities.expense.InstallmentExpense;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.entities.income.Income;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PaginatedEndpointsSortFieldsTest {

    static Stream<Arguments> controllers() {
        return Stream.of(
                Arguments.of(ExpenseController.class, Expense.class),
                Arguments.of(FixedExpenseController.class, FixedExpense.class),
                Arguments.of(InstallmentExpenseController.class, InstallmentExpense.class),
                Arguments.of(ExpenseCategoryController.class, ExpenseCategory.class),
                Arguments.of(IncomeController.class, Income.class),
                Arguments.of(FixedIncomeController.class, FixedIncome.class),
                Arguments.of(IncomeCategoryController.class, IncomeCategory.class),
                Arguments.of(BankAccountController.class, BankAccount.class),
                Arguments.of(PaymentMethodController.class, PaymentMethod.class)
        );
    }

    @ParameterizedTest
    @MethodSource("controllers")
    void everyPageableParameter_shouldDeclareWhitelistOfExistingEntityProperties(Class<?> controller, Class<?> entity) {
        long pageableParameters = 0;

        for (Method method : controller.getDeclaredMethods()) {
            for (Parameter parameter : method.getParameters()) {
                if (!Pageable.class.isAssignableFrom(parameter.getType())) {
                    continue;
                }
                pageableParameters++;

                SortableBy sortableBy = parameter.getAnnotation(SortableBy.class);
                assertThat(sortableBy).as("%s#%s", controller.getSimpleName(), method.getName()).isNotNull();

                for (String property : SortableByArgumentResolver.allowedProperties(sortableBy)) {
                    assertThat(hasField(entity, property))
                            .as("%s.%s", entity.getSimpleName(), property).isTrue();
                }
                assertThat(SortableByArgumentResolver.allowedProperties(sortableBy))
                        .contains(sortableBy.defaultSort().split(",")[0]);
            }
        }

        assertThat(pageableParameters).isPositive();
    }

    private static boolean hasField(Class<?> type, String name) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            if (Arrays.stream(c.getDeclaredFields()).map(Field::getName).anyMatch(name::equals)) {
                return true;
            }
        }
        return false;
    }
}
