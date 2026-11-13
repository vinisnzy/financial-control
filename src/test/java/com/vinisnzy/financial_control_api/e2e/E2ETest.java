package com.vinisnzy.financial_control_api.e2e;

import com.jayway.jsonpath.JsonPath;
import com.vinisnzy.financial_control_api.TestcontainersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
public abstract class E2ETest {

    protected static final String PASSWORD = "12345678";

    @Autowired
    protected MockMvc mvc;

    protected record Session(String email, String accessToken, String refreshToken) {
    }

    protected Session signUpAndSignIn() throws Exception {
        String email = "mail-" + UUID.randomUUID() + "@test.com";
        String body = credentials(email, PASSWORD);

        mvc.perform(post("/auth/sign-up").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        String response = mvc.perform(post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return new Session(email, JsonPath.read(response, "$.accessToken"), JsonPath.read(response, "$.refreshToken"));
    }

    protected ResultActions signUp(String email, String password) throws Exception {
        return mvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    protected ResultActions signIn(String email, String password) throws Exception {
        return mvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    protected ResultActions refresh(String refreshToken) throws Exception {
        return mvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshTokenBody(refreshToken)));
    }

    protected ResultActions logout(String refreshToken) throws Exception {
        return mvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshTokenBody(refreshToken)));
    }

    protected ResultActions authed(MockHttpServletRequestBuilder req, String token) throws Exception {
        return mvc.perform(req.header("Authorization", "Bearer " + token));
    }

    protected MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String body) {
        return req.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    protected String idOf(ResultActions created) throws Exception {
        String response = created
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    protected String createBankAccount(String token) throws Exception {
        return idOf(authed(json(post("/bank-accounts"), """
                {"name": "Nubank", "initialBalance": 1000.00}
                """), token));
    }

    protected String createPaymentMethod(String token, String bankAccountId) throws Exception {
        return idOf(authed(json(post("/payment-methods"), """
                {"name": "Nubank credit", "type": "Credit card", "bankAccountId": "%s"}
                """.formatted(bankAccountId)), token));
    }

    protected String createExpenseCategory(String token) throws Exception {
        return idOf(authed(json(post("/expense-categories"), """
                {"name": "Food", "color": "#FF5733"}
                """), token));
    }

    protected String createIncomeCategory(String token) throws Exception {
        return idOf(authed(json(post("/income-categories"), """
                {"name": "Salary", "color": "#00AA55"}
                """), token));
    }

    protected String createExpense(String token, String categoryId, String paymentMethodId, String name, String dueDate) throws Exception {
        return idOf(authed(json(post("/expenses"), """
                {"name": "%s", "amount": 250.00, "expenseCategoryId": "%s",
                 "paymentMethodId": "%s", "dueDate": "%s"}
                """.formatted(name, categoryId, paymentMethodId, dueDate)), token));
    }

    protected String createIncome(String token, String categoryId, String bankAccountId, String name, String expectedAt) throws Exception {
        return idOf(authed(json(post("/incomes"), """
                {"name": "%s", "amount": 5000.00, "incomeCategoryId": "%s",
                 "bankAccountId": "%s", "expectedAt": "%s"}
                """.formatted(name, categoryId, bankAccountId, expectedAt)), token));
    }

    private String credentials(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    private String refreshTokenBody(String refreshToken) {
        return """
                {"refreshToken": "%s"}
                """.formatted(refreshToken);
    }
}
