package com.vinisnzy.financial_control_api.e2e;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExpenseFlowE2ETest extends E2ETest {

    @Test
    void expenseLifecycle() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(token);
        String paymentMethodId = createPaymentMethod(token, accountId);
        String categoryId = createExpenseCategory(token);

        String expenseId = createExpense(token, categoryId, paymentMethodId, "Groceries", "2026-01-10");

        authed(get("/expenses/{id}", expenseId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Groceries"))
                .andExpect(jsonPath("$.expenseCategoryId").value(categoryId))
                .andExpect(jsonPath("$.paymentMethodId").value(paymentMethodId))
                .andExpect(jsonPath("$.paidAt").doesNotExist());

        authed(get("/expenses").param("start", "2026-01-01").param("end", "2026-01-31"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(get("/expenses").param("start", "2026-02-01").param("end", "2026-02-28"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        authed(get("/expenses/search").param("name", "groc"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(get("/expenses/expense-category/{id}", categoryId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(get("/expenses/payment-method/{id}", paymentMethodId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(json(patch("/expenses/{id}", expenseId), """
                {"paidAt": "2026-01-10"}
                """), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidAt").value("2026-01-10"));

        authed(delete("/expenses/{id}", expenseId), token)
                .andExpect(status().isNoContent());

        authed(get("/expenses/{id}", expenseId), token)
                .andExpect(status().isNotFound());
    }

    @Test
    void listExpenses_withOnlyOneDateOfThePeriod_shouldReturn400() throws Exception {
        String token = signUpAndSignIn().accessToken();

        authed(get("/expenses").param("start", "2026-01-01"), token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void listExpenses_withStartAfterEnd_shouldReturn400() throws Exception {
        String token = signUpAndSignIn().accessToken();

        authed(get("/expenses").param("start", "2026-02-01").param("end", "2026-01-01"), token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void deleteBankAccount_withPaymentMethod_shouldReturn409() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(token);
        createPaymentMethod(token, accountId);

        authed(delete("/bank-accounts/{id}", accountId), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"));
    }
}
