package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseResponse;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.expense.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest extends ControllerTest {

    @MockitoBean
    ExpenseService service;

    private UUID expenseId;
    private UUID categoryId;
    private UUID paymentMethodId;
    private ExpenseResponse response;
    private PageResponse<ExpenseResponse> page;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
        response = new ExpenseResponse(expenseId, "Groceries", new BigDecimal("250.00"), categoryId, paymentMethodId,
                LocalDate.of(2026, 1, 10), null, null, null, null, Instant.now(), null);
        page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
    }

    @Test
    void getAll_withoutPeriod_shouldReturn200WithPage() throws Exception {
        when(service.getAll(isNull(), isNull(), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expenses").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Groceries"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAll_withPeriod_shouldPassDatesToService() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(service.getAll(eq(start), eq(end), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expenses").param("start", "2026-01-01").param("end", "2026-01-31").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(expenseId.toString()));
    }

    @Test
    void getAll_withInvalidPeriod_shouldReturn400() throws Exception {
        when(service.getAll(any(), any(), eq(authUser.getId()), any()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_PERIOD, "Start date must be before end date."));

        mvc.perform(get("/expenses").param("start", "2026-02-01").param("end", "2026-01-01").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void getAll_withMalformedDate_shouldReturn400() throws Exception {
        mvc.perform(get("/expenses").param("start", "31-01-2026").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(expenseId, authUser.getId())).thenReturn(response);

        mvc.perform(get("/expenses/{id}", expenseId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(expenseId, authUser.getId())).thenThrow(NotFoundException.of("Expense", expenseId));

        mvc.perform(get("/expenses/{id}", expenseId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/expenses/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByName_shouldReturn200WithPage() throws Exception {
        when(service.getByName(eq("gro"), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expenses/search").param("name", "gro").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Groceries"));
    }

    @Test
    void getByName_withoutNameParam_shouldReturn400() throws Exception {
        mvc.perform(get("/expenses/search").with(user(authUser)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getByExpenseCategory_shouldReturn200WithPage() throws Exception {
        when(service.getByExpenseCategory(eq(categoryId), isNull(), isNull(), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expenses/expense-category/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].expenseCategoryId").value(categoryId.toString()));
    }

    @Test
    void getByExpenseCategory_withPeriod_shouldPassDatesToService() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(service.getByExpenseCategory(eq(categoryId), eq(start), eq(end), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expenses/expense-category/{id}", categoryId)
                        .param("start", "2026-01-01").param("end", "2026-01-31").with(user(authUser)))
                .andExpect(status().isOk());
    }

    @Test
    void getByExpenseCategory_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/expenses/expense-category/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByPaymentMethod_shouldReturn200WithPage() throws Exception {
        when(service.getByPaymentMethod(eq(paymentMethodId), isNull(), isNull(), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expenses/payment-method/{id}", paymentMethodId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentMethodId").value(paymentMethodId.toString()));
    }

    @Test
    void getByPaymentMethod_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/expenses/payment-method/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByInstallmentExpense_shouldReturn200WithPage() throws Exception {
        UUID installmentId = UUID.randomUUID();
        when(service.getByInstallmentExpense(eq(installmentId), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expenses/installment-expense/{id}", installmentId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getByInstallmentExpense_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/expenses/installment-expense/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Groceries", "amount": 250.00, "expenseCategoryId": "%s",
                                 "paymentMethodId": "%s", "dueDate": "2026-01-10"}
                                """.formatted(categoryId, paymentMethodId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Groceries"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem(field)));

        verifyNoInteractions(service);
    }

    static Stream<Arguments> invalidCreateBodies() {
        String category = UUID.randomUUID().toString();
        String payment = UUID.randomUUID().toString();
        String ids = """
                "expenseCategoryId": "%s", "paymentMethodId": "%s", "dueDate": "2026-01-10"
                """.formatted(category, payment);
        String future = LocalDate.now().plusDays(5).toString();
        return Stream.of(
                Arguments.of("blank name", "{\"name\": \"\", \"amount\": 10, " + ids + "}", "name"),
                Arguments.of("name above 100 characters", "{\"name\": \"" + "a".repeat(101) + "\", \"amount\": 10, " + ids + "}", "name"),
                Arguments.of("missing amount", "{\"name\": \"Groceries\", " + ids + "}", "amount"),
                Arguments.of("zero amount", "{\"name\": \"Groceries\", \"amount\": 0, " + ids + "}", "amount"),
                Arguments.of("three decimal places", "{\"name\": \"Groceries\", \"amount\": 1.999, " + ids + "}", "amount"),
                Arguments.of("missing expenseCategoryId", """
                        {"name": "Groceries", "amount": 10, "paymentMethodId": "%s", "dueDate": "2026-01-10"}""".formatted(payment), "expenseCategoryId"),
                Arguments.of("missing paymentMethodId", """
                        {"name": "Groceries", "amount": 10, "expenseCategoryId": "%s", "dueDate": "2026-01-10"}""".formatted(category), "paymentMethodId"),
                Arguments.of("missing dueDate", """
                        {"name": "Groceries", "amount": 10, "expenseCategoryId": "%s", "paymentMethodId": "%s"}""".formatted(category, payment), "dueDate"),
                Arguments.of("paidAt in the future", """
                        {"name": "Groceries", "amount": 10, "expenseCategoryId": "%s", "paymentMethodId": "%s", "dueDate": "2026-01-10", "paidAt": "%s"}""".formatted(category, payment, future), "paidAt")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(expenseId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/expenses/{id}", expenseId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Market"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/expenses/{id}", expenseId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem(field)));

        verifyNoInteractions(service);
    }

    static Stream<Arguments> invalidUpdateBodies() {
        return Stream.of(
                Arguments.of("blank name", """
                        {"name": "   "}""", "name"),
                Arguments.of("name above 100 characters", """
                        {"name": "%s"}""".formatted("a".repeat(101)), "name"),
                Arguments.of("zero amount", """
                        {"amount": 0}""", "amount"),
                Arguments.of("paidAt in the future", """
                        {"paidAt": "%s"}""".formatted(LocalDate.now().plusDays(5)), "paidAt")
        );
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/expenses/{id}", expenseId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(expenseId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/expenses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
