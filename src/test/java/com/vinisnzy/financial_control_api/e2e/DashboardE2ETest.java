package com.vinisnzy.financial_control_api.e2e;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardE2ETest extends E2ETest {

    @Test
    void summary_shouldSeparatePlannedFromRealizedAndIgnoreOtherMonths() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(token);
        String paymentMethodId = createPaymentMethod(token, accountId);
        String expenseCategoryId = createExpenseCategory(token);
        String incomeCategoryId = createIncomeCategory(token);

        // March: income of 5000 (received), two expenses of 250 (only one paid)
        String income = createIncome(token, incomeCategoryId, accountId, "Salary", "2026-03-05");
        authed(json(patch("/incomes/{id}", income), """
                {"receivedAt": "2026-03-05"}
                """), token).andExpect(status().isOk());

        String paidExpense = createExpense(token, expenseCategoryId, paymentMethodId, "Rent", "2026-03-10");
        createExpense(token, expenseCategoryId, paymentMethodId, "Food", "2026-03-20");
        authed(json(patch("/expenses/{id}", paidExpense), """
                {"paidAt": "2026-03-10"}
                """), token).andExpect(status().isOk());

        // April: must not appear in March
        createIncome(token, incomeCategoryId, accountId, "April salary", "2026-04-05");
        createExpense(token, expenseCategoryId, paymentMethodId, "April rent", "2026-04-10");

        authed(get("/dashboard/summary").param("month", "2026-03"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-03"))
                .andExpect(jsonPath("$.plannedIncome").value(5000.0))
                .andExpect(jsonPath("$.receivedIncome").value(5000.0))
                .andExpect(jsonPath("$.plannedExpense").value(500.0))
                .andExpect(jsonPath("$.paidExpense").value(250.0))
                .andExpect(jsonPath("$.plannedBalance").value(4500.0))
                .andExpect(jsonPath("$.realizedBalance").value(4750.0));
    }

    @Test
    void expensesByCategory_shouldGroupWithPercentageOrderedByTotal() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String paymentMethodId = createPaymentMethod(token, createBankAccount(token));
        String food = createExpenseCategory(token, "Food");
        String rent = createExpenseCategory(token, "Rent");

        createExpenseWithAmount(token, food, paymentMethodId, "300.00", "2026-03-02");
        createExpenseWithAmount(token, rent, paymentMethodId, "900.00", "2026-03-05");
        // outside the period
        createExpenseWithAmount(token, rent, paymentMethodId, "999.00", "2026-04-05");

        authed(get("/dashboard/expenses-by-category").param("start", "2026-03-01").param("end", "2026-03-31"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryId").value(rent))
                .andExpect(jsonPath("$[0].categoryName").value("Rent"))
                .andExpect(jsonPath("$[0].total").value(900.0))
                .andExpect(jsonPath("$[0].percentage").value(75.0))
                .andExpect(jsonPath("$[1].categoryName").value("Food"))
                .andExpect(jsonPath("$[1].percentage").value(25.0));
    }

    @Test
    void monthlyCashFlow_shouldReturnOneEntryPerMonthFillingEmptyOnesWithZero() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(token);
        String paymentMethodId = createPaymentMethod(token, createBankAccount(token));
        YearMonth current = YearMonth.now();

        createIncome(token, createIncomeCategory(token), accountId, "Salary", current.atDay(1).toString());
        createExpense(token, createExpenseCategory(token), paymentMethodId, "Rent", current.atDay(1).toString());

        authed(get("/dashboard/monthly-cash-flow").param("months", "2"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].month").value(current.minusMonths(1).toString()))
                .andExpect(jsonPath("$[0].income").value(0.0))
                .andExpect(jsonPath("$[0].balance").value(0.0))
                .andExpect(jsonPath("$[1].month").value(current.toString()))
                .andExpect(jsonPath("$[1].income").value(5000.0))
                .andExpect(jsonPath("$[1].expense").value(250.0))
                .andExpect(jsonPath("$[1].balance").value(4750.0));
    }

    @Test
    void installmentsProjection_shouldCommitUnpaidInstallmentsByMonth() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String categoryId = createExpenseCategory(token);
        String paymentMethodId = createPaymentMethod(token, createBankAccount(token));
        YearMonth next = YearMonth.now().plusMonths(1);

        createInstallment(token, categoryId, paymentMethodId, next);

        authed(get("/dashboard/installments-projection").param("months", "3"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].month").value(next.toString()))
                .andExpect(jsonPath("$[0].committed").value(100.0))
                .andExpect(jsonPath("$[1].committed").value(100.0))
                .andExpect(jsonPath("$[2].committed").value(100.0));
    }

    @Test
    void byCategory_withStartAfterEnd_shouldReturn400() throws Exception {
        String token = signUpAndSignIn().accessToken();

        authed(get("/dashboard/expenses-by-category").param("start", "2026-04-01").param("end", "2026-03-01"), token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void otherUser_neverSeesMyDashboardData() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String accountId = createBankAccount(tokenA);
        String paymentMethodId = createPaymentMethod(tokenA, accountId);
        String expenseCategoryId = createExpenseCategory(tokenA);
        YearMonth current = YearMonth.now();

        createIncome(tokenA, createIncomeCategory(tokenA), accountId, "Salary", current.atDay(2).toString());
        createExpense(tokenA, expenseCategoryId, paymentMethodId, "Rent", current.atDay(3).toString());
        createInstallment(tokenA, expenseCategoryId, paymentMethodId, current.plusMonths(1));

        String start = current.atDay(1).toString();
        String end = current.atEndOfMonth().toString();

        // sanity: user A sees their own data
        authed(get("/dashboard/summary").param("month", current.toString()), tokenA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedIncome").value(5000.0))
                .andExpect(jsonPath("$.plannedExpense").value(250.0));

        // user B sees nothing
        authed(get("/dashboard/summary").param("month", current.toString()), tokenB)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedIncome").value(0.0))
                .andExpect(jsonPath("$.plannedExpense").value(0.0));

        authed(get("/dashboard/expenses-by-category").param("start", start).param("end", end), tokenB)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        authed(get("/dashboard/installments-projection").param("months", "1"), tokenB)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].committed").value(0.0));
    }

    @Test
    void dashboard_withoutToken_shouldReturn401() throws Exception {
        mvc.perform(get("/dashboard/summary")).andExpect(status().isUnauthorized());
    }

    private String createExpenseCategory(String token, String name) throws Exception {
        return idOf(authed(json(post("/expense-categories"), """
                {"name": "%s", "color": "#FF5733"}
                """.formatted(name)), token));
    }

    private String createExpenseWithAmount(String token, String categoryId, String paymentMethodId,
                                           String amount, String dueDate) throws Exception {
        return idOf(authed(json(post("/expenses"), """
                {"name": "expense", "amount": %s, "expenseCategoryId": "%s",
                 "paymentMethodId": "%s", "dueDate": "%s"}
                """.formatted(amount, categoryId, paymentMethodId, dueDate)), token));
    }

    private String createInstallment(String token, String categoryId, String paymentMethodId,
                                     YearMonth firstMonth) throws Exception {
        return idOf(authed(json(post("/installment-expenses"), """
                {"name": "Phone", "totalAmount": 300.00, "installments": 3,
                 "expenseCategoryId": "%s", "paymentMethodId": "%s", "firstDueDate": "%s"}
                """.formatted(categoryId, paymentMethodId, firstMonth.atDay(1))), token));
    }
}
