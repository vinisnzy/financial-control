package com.vinisnzy.financial_control_api.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InstallmentExpenseE2ETest extends E2ETest {

    private String token;
    private String categoryId;
    private String paymentMethodId;

    @BeforeEach
    void setUp() throws Exception {
        token = signUpAndSignIn().accessToken();
        categoryId = createExpenseCategory(token);
        paymentMethodId = createPaymentMethod(token, createBankAccount(token));
    }

    private String createInstallment(String totalAmount, int installments) throws Exception {
        return idOf(authed(json(post("/installment-expenses"), """
                {"name": "Phone", "totalAmount": %s, "installments": %d,
                 "expenseCategoryId": "%s", "paymentMethodId": "%s", "firstDueDate": "2026-01-31"}
                """.formatted(totalAmount, installments, categoryId, paymentMethodId)), token));
    }

    @Test
    void create_shouldGenerateOneExpensePerInstallment() throws Exception {
        String installmentId = createInstallment("100.00", 3);

        String response = authed(get("/expenses/installment-expense/{id}", installmentId).param("sort", "dueDate"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andReturn().getResponse().getContentAsString();

        List<Number> amounts = JsonPath.read(response, "$.content[*].amount");
        List<String> dueDates = JsonPath.read(response, "$.content[*].dueDate");
        List<Integer> numbers = JsonPath.read(response, "$.content[*].installmentNumber");

        assertThat(amounts.stream().mapToDouble(Number::doubleValue).sum()).isEqualTo(100.00);
        assertThat(dueDates).containsExactly("2026-01-31", "2026-02-28", "2026-03-31");
        assertThat(numbers).containsExactly(1, 2, 3);
    }

    @Test
    void create_shouldPutRoundingRemainderOnTheLastInstallment() throws Exception {
        String installmentId = createInstallment("100.00", 3);

        String response = authed(get("/expenses/installment-expense/{id}", installmentId).param("sort", "dueDate"), token)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Number> amounts = JsonPath.read(response, "$.content[*].amount");

        assertThat(amounts.get(0).doubleValue()).isEqualTo(33.33);
        assertThat(amounts.get(1).doubleValue()).isEqualTo(33.33);
        assertThat(amounts.get(2).doubleValue()).isEqualTo(33.34);
    }

    @Test
    void update_scheduleWithoutPaidInstallments_shouldRegenerateExpenses() throws Exception {
        String installmentId = createInstallment("100.00", 3);

        authed(json(patch("/installment-expenses/{id}", installmentId), """
                {"installments": 4}
                """), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installments").value(4));

        authed(get("/expenses/installment-expense/{id}", installmentId), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void update_scheduleWithPaidInstallment_shouldReturn409() throws Exception {
        String installmentId = createInstallment("100.00", 3);
        String firstExpenseId = firstExpenseOf(installmentId);

        authed(json(patch("/expenses/{id}", firstExpenseId), """
                {"paidAt": "2026-01-31"}
                """), token)
                .andExpect(status().isOk());

        authed(json(patch("/installment-expenses/{id}", installmentId), """
                {"installments": 4}
                """), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSTALLMENT_HAS_PAID_PARTS"));

        authed(get("/expenses/installment-expense/{id}", installmentId), token)
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void update_nameWithPaidInstallment_shouldReturn200() throws Exception {
        String installmentId = createInstallment("100.00", 3);
        String firstExpenseId = firstExpenseOf(installmentId);

        authed(json(patch("/expenses/{id}", firstExpenseId), """
                {"paidAt": "2026-01-31"}
                """), token)
                .andExpect(status().isOk());

        authed(json(patch("/installment-expenses/{id}", installmentId), """
                {"name": "Laptop"}
                """), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void delete_shouldRemoveItsExpenses() throws Exception {
        String installmentId = createInstallment("100.00", 3);
        String firstExpenseId = firstExpenseOf(installmentId);

        authed(delete("/installment-expenses/{id}", installmentId), token)
                .andExpect(status().isNoContent());

        authed(get("/installment-expenses/{id}", installmentId), token)
                .andExpect(status().isNotFound());

        authed(get("/expenses/{id}", firstExpenseId), token)
                .andExpect(status().isNotFound());
    }

    private String firstExpenseOf(String installmentId) throws Exception {
        String response = authed(get("/expenses/installment-expense/{id}", installmentId).param("sort", "dueDate"), token)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.content[0].id");
    }
}
