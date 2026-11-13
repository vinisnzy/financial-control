package com.vinisnzy.financial_control_api.e2e;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IncomeFlowE2ETest extends E2ETest {

    @Test
    void incomeLifecycle() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(token);
        String categoryId = createIncomeCategory(token);

        String incomeId = createIncome(token, categoryId, accountId, "Salary", "2026-01-05");

        authed(get("/incomes/{id}", incomeId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salary"))
                .andExpect(jsonPath("$.incomeCategoryId").value(categoryId))
                .andExpect(jsonPath("$.bankAccountId").value(accountId))
                .andExpect(jsonPath("$.receivedAt").doesNotExist());

        authed(get("/incomes").param("start", "2026-01-01").param("end", "2026-01-31"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(get("/incomes").param("start", "2026-02-01").param("end", "2026-02-28"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        authed(get("/incomes/search").param("name", "sal"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(get("/incomes/bank-account/{id}", accountId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(get("/incomes/income-category/{id}", categoryId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(json(patch("/incomes/{id}", incomeId), """
                {"receivedAt": "2026-01-05"}
                """), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receivedAt").value("2026-01-05"));

        authed(delete("/incomes/{id}", incomeId), token)
                .andExpect(status().isNoContent());

        authed(get("/incomes/{id}", incomeId), token)
                .andExpect(status().isNotFound());
    }

    @Test
    void listIncomes_withStartAfterEnd_shouldReturn400() throws Exception {
        String token = signUpAndSignIn().accessToken();

        authed(get("/incomes").param("start", "2026-02-01").param("end", "2026-01-01"), token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }
}
