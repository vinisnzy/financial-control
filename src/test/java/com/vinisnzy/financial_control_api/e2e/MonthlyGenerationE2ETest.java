package com.vinisnzy.financial_control_api.e2e;

import com.vinisnzy.financial_control_api.jobs.MonthlyExpenseGeneratorJob;
import com.vinisnzy.financial_control_api.jobs.MonthlyIncomeGeneratorJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.time.YearMonth;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MonthlyGenerationE2ETest extends E2ETest {

    private static final YearMonth MONTH = YearMonth.now(ZoneId.of("America/Sao_Paulo"));

    @Autowired
    MonthlyExpenseGeneratorJob expenseJob;

    @Autowired
    MonthlyIncomeGeneratorJob incomeJob;

    private String createFixedExpense(String token, int dueDay, String startDate, String endDate) throws Exception {
        String end = endDate == null ? "" : ", \"endDate\": \"%s\"".formatted(endDate);
        String categoryId = createExpenseCategory(token);
        String paymentMethodId = createPaymentMethod(token, createBankAccount(token));
        return idOf(authed(json(post("/fixed-expenses"), """
                {"name": "Rent", "amount": 1500.00, "expenseCategoryId": "%s", "paymentMethodId": "%s",
                 "dueDay": %d, "startDate": "%s"%s}
                """.formatted(categoryId, paymentMethodId, dueDay, startDate, end)), token));
    }

    private String createFixedIncome(String token, int payday, String startDate) throws Exception {
        String categoryId = createIncomeCategory(token);
        String accountId = createBankAccount(token);
        return idOf(authed(json(post("/fixed-incomes"), """
                {"name": "Salary", "amount": 5000.00, "incomeCategoryId": "%s", "bankAccountId": "%s",
                 "payday": %d, "startDate": "%s"}
                """.formatted(categoryId, accountId, payday, startDate)), token));
    }

    private ResultActions expensesOfCurrentMonth(String token) throws Exception {
        return authed(get("/expenses")
                .param("start", MONTH.atDay(1).toString())
                .param("end", MONTH.atEndOfMonth().toString()), token);
    }

    private ResultActions incomesOfCurrentMonth(String token) throws Exception {
        return authed(get("/incomes")
                .param("start", MONTH.atDay(1).toString())
                .param("end", MONTH.atEndOfMonth().toString()), token);
    }

    @Test
    void expenseJob_shouldGenerateExpenseFromFixedExpense() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String fixedExpenseId = createFixedExpense(token, 15, MONTH.minusMonths(1).atDay(1).toString(), null);

        expenseJob.generate("test");

        expensesOfCurrentMonth(token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Rent"))
                .andExpect(jsonPath("$.content[0].fixedExpenseId").value(fixedExpenseId))
                .andExpect(jsonPath("$.content[0].dueDate").value(MONTH.atDay(15).toString()))
                .andExpect(jsonPath("$.content[0].paidAt").doesNotExist());
    }

    @Test
    void expenseJob_runTwice_shouldNotDuplicateExpense() throws Exception {
        String token = signUpAndSignIn().accessToken();
        createFixedExpense(token, 15, MONTH.minusMonths(1).atDay(1).toString(), null);

        expenseJob.generate("first run");
        expenseJob.generate("second run");

        expensesOfCurrentMonth(token)
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void expenseJob_withDueDay31_shouldUseLastDayOfMonth() throws Exception {
        String token = signUpAndSignIn().accessToken();
        createFixedExpense(token, 31, MONTH.minusMonths(1).atDay(1).toString(), null);

        expenseJob.generate("test");

        expensesOfCurrentMonth(token)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].dueDate").value(MONTH.atEndOfMonth().toString()));
    }

    @Test
    void expenseJob_withFixedExpenseEndedBeforeMonth_shouldNotGenerate() throws Exception {
        String token = signUpAndSignIn().accessToken();
        createFixedExpense(token, 15, MONTH.minusMonths(3).atDay(1).toString(), MONTH.minusMonths(1).atEndOfMonth().toString());

        expenseJob.generate("test");

        expensesOfCurrentMonth(token)
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void expenseJob_withFixedExpenseStartingNextMonth_shouldNotGenerate() throws Exception {
        String token = signUpAndSignIn().accessToken();
        createFixedExpense(token, 15, MONTH.plusMonths(1).atDay(1).toString(), null);

        expenseJob.generate("test");

        expensesOfCurrentMonth(token)
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void expenseJob_shouldGenerateEachExpenseForItsOwner() throws Exception {
        String tokenA = signUpAndSignIn().accessToken();
        String tokenB = signUpAndSignIn().accessToken();
        String fixedA = createFixedExpense(tokenA, 10, MONTH.minusMonths(1).atDay(1).toString(), null);
        String fixedB = createFixedExpense(tokenB, 20, MONTH.minusMonths(1).atDay(1).toString(), null);

        expenseJob.generate("test");

        expensesOfCurrentMonth(tokenA)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fixedExpenseId").value(fixedA));
        expensesOfCurrentMonth(tokenB)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fixedExpenseId").value(fixedB));
    }

    @Test
    void incomeJob_shouldGenerateIncomeFromFixedIncome() throws Exception {
        String token = signUpAndSignIn().accessToken();
        String fixedIncomeId = createFixedIncome(token, 5, MONTH.minusMonths(1).atDay(1).toString());

        incomeJob.generate("test");

        incomesOfCurrentMonth(token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Salary"))
                .andExpect(jsonPath("$.content[0].fixedIncomeId").value(fixedIncomeId))
                .andExpect(jsonPath("$.content[0].expectedAt").value(MONTH.atDay(5).toString()))
                .andExpect(jsonPath("$.content[0].receivedAt").doesNotExist());
    }

    @Test
    void incomeJob_runTwice_shouldNotDuplicateIncome() throws Exception {
        String token = signUpAndSignIn().accessToken();
        createFixedIncome(token, 5, MONTH.minusMonths(1).atDay(1).toString());

        incomeJob.generate("first run");
        incomeJob.generate("second run");

        incomesOfCurrentMonth(token)
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void incomeJob_withPayday31_shouldUseLastDayOfMonth() throws Exception {
        String token = signUpAndSignIn().accessToken();
        createFixedIncome(token, 31, MONTH.minusMonths(1).atDay(1).toString());

        incomeJob.generate("test");

        incomesOfCurrentMonth(token)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].expectedAt").value(MONTH.atEndOfMonth().toString()));
    }
}
