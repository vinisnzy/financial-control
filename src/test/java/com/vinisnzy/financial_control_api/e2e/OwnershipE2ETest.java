package com.vinisnzy.financial_control_api.e2e;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OwnershipE2ETest extends E2ETest {

    @Test
    void otherUser_cannotReadMyBankAccount() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(tokenA);

        authed(get("/bank-accounts/{id}", accountId), tokenB)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void otherUser_cannotUpdateMyBankAccount() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(tokenA);

        authed(json(patch("/bank-accounts/{id}", accountId), """
                {"name": "Hacked"}
                """), tokenB)
                .andExpect(status().isNotFound());

        authed(get("/bank-accounts/{id}", accountId), tokenA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nubank"));
    }

    @Test
    void otherUser_cannotDeleteMyBankAccount() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(tokenA);

        authed(delete("/bank-accounts/{id}", accountId), tokenB)
                .andExpect(status().isNotFound());

        authed(get("/bank-accounts/{id}", accountId), tokenA)
                .andExpect(status().isOk());
    }

    @Test
    void otherUser_doesNotSeeMyBankAccountsInList() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        createBankAccount(tokenA);

        authed(get("/bank-accounts"), tokenA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        authed(get("/bank-accounts"), tokenB)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void otherUser_cannotCreatePaymentMethodWithMyBankAccount() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(tokenA);

        authed(json(post("/payment-methods"), """
                {"name": "Intruder", "type": "Debit card", "bankAccountId": "%s"}
                """.formatted(accountId)), tokenB)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void otherUser_cannotCreateExpenseWithMyCategoryAndPaymentMethod() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String paymentMethodId = createPaymentMethod(tokenA, createBankAccount(tokenA));
        String categoryId = createExpenseCategory(tokenA);

        authed(json(post("/expenses"), """
                {"name": "Intruder", "amount": 10.00, "expenseCategoryId": "%s",
                 "paymentMethodId": "%s", "dueDate": "2026-01-10"}
                """.formatted(categoryId, paymentMethodId)), tokenB)
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUser_cannotReadMyExpense() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String paymentMethodId = createPaymentMethod(tokenA, createBankAccount(tokenA));
        String categoryId = createExpenseCategory(tokenA);
        String expenseId = createExpense(tokenA, categoryId, paymentMethodId, "Groceries", "2026-01-10");

        authed(get("/expenses/{id}", expenseId), tokenB)
                .andExpect(status().isNotFound());
    }
}
