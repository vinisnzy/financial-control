package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.responses.FixedExpenseResponse;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.expense.FixedExpenseService;
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

@WebMvcTest(FixedExpenseController.class)
class FixedExpenseControllerTest extends ControllerTest {

    @MockitoBean
    FixedExpenseService service;

    private UUID fixedExpenseId;
    private UUID categoryId;
    private UUID paymentMethodId;
    private FixedExpenseResponse response;

    @BeforeEach
    void setUp() {
        fixedExpenseId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
        response = new FixedExpenseResponse(fixedExpenseId, "Rent", new BigDecimal("1500.00"), categoryId,
                paymentMethodId, (short) 5, LocalDate.of(2026, 1, 1), null, Instant.now(), null);
    }

    private String validBody() {
        return """
                {"name": "Rent", "amount": 1500.00, "expenseCategoryId": "%s", "paymentMethodId": "%s",
                 "dueDay": 5, "startDate": "2026-01-01"}
                """.formatted(categoryId, paymentMethodId);
    }

    @Test
    void getAll_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getAll(eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/fixed-expenses").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Rent"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(fixedExpenseId, authUser.getId())).thenReturn(response);

        mvc.perform(get("/fixed-expenses/{id}", fixedExpenseId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fixedExpenseId.toString()));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(fixedExpenseId, authUser.getId()))
                .thenThrow(NotFoundException.of("Fixed expense", fixedExpenseId));

        mvc.perform(get("/fixed-expenses/{id}", fixedExpenseId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/fixed-expenses/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByExpenseCategory_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getByExpenseCategory(eq(categoryId), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/fixed-expenses/expense-category/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].expenseCategoryId").value(categoryId.toString()));
    }

    @Test
    void getByExpenseCategory_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/fixed-expenses/expense-category/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/fixed-expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Rent"))
                .andExpect(jsonPath("$.dueDay").value(5));
    }

    @Test
    void create_withEndDateEqualToStartDate_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/fixed-expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Rent", "amount": 1500.00, "expenseCategoryId": "%s", "paymentMethodId": "%s",
                                 "dueDay": 5, "startDate": "2026-01-01", "endDate": "2026-01-01"}
                                """.formatted(categoryId, paymentMethodId)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/fixed-expenses")
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
        String base = """
                "expenseCategoryId": "%s", "paymentMethodId": "%s", "dueDay": 5, "startDate": "2026-01-01"
                """.formatted(category, payment);
        return Stream.of(
                Arguments.of("blank name", "{\"name\": \"\", \"amount\": 10, " + base + "}", "name"),
                Arguments.of("name above 100 characters", "{\"name\": \"" + "a".repeat(101) + "\", \"amount\": 10, " + base + "}", "name"),
                Arguments.of("missing amount", "{\"name\": \"Rent\", " + base + "}", "amount"),
                Arguments.of("zero amount", "{\"name\": \"Rent\", \"amount\": 0, " + base + "}", "amount"),
                Arguments.of("three decimal places", "{\"name\": \"Rent\", \"amount\": 1.999, " + base + "}", "amount"),
                Arguments.of("missing expenseCategoryId", """
                        {"name": "Rent", "amount": 10, "paymentMethodId": "%s", "dueDay": 5, "startDate": "2026-01-01"}""".formatted(payment), "expenseCategoryId"),
                Arguments.of("missing paymentMethodId", """
                        {"name": "Rent", "amount": 10, "expenseCategoryId": "%s", "dueDay": 5, "startDate": "2026-01-01"}""".formatted(category), "paymentMethodId"),
                Arguments.of("missing dueDay", """
                        {"name": "Rent", "amount": 10, "expenseCategoryId": "%s", "paymentMethodId": "%s", "startDate": "2026-01-01"}""".formatted(category, payment), "dueDay"),
                Arguments.of("dueDay below 1", """
                        {"name": "Rent", "amount": 10, "expenseCategoryId": "%s", "paymentMethodId": "%s", "dueDay": 0, "startDate": "2026-01-01"}""".formatted(category, payment), "dueDay"),
                Arguments.of("dueDay above 31", """
                        {"name": "Rent", "amount": 10, "expenseCategoryId": "%s", "paymentMethodId": "%s", "dueDay": 32, "startDate": "2026-01-01"}""".formatted(category, payment), "dueDay"),
                Arguments.of("missing startDate", """
                        {"name": "Rent", "amount": 10, "expenseCategoryId": "%s", "paymentMethodId": "%s", "dueDay": 5}""".formatted(category, payment), "startDate"),
                Arguments.of("endDate before startDate", """
                        {"name": "Rent", "amount": 10, "expenseCategoryId": "%s", "paymentMethodId": "%s", "dueDay": 5, "startDate": "2026-02-01", "endDate": "2026-01-01"}""".formatted(category, payment), "endDateValid")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/fixed-expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(fixedExpenseId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/fixed-expenses/{id}", fixedExpenseId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Internet"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fixedExpenseId.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/fixed-expenses/{id}", fixedExpenseId)
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
                Arguments.of("dueDay above 31", """
                        {"dueDay": 32}""", "dueDay"),
                Arguments.of("endDate before startDate", """
                        {"startDate": "2026-02-01", "endDate": "2026-01-01"}""", "endDateValid")
        );
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/fixed-expenses/{id}", fixedExpenseId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(fixedExpenseId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/fixed-expenses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
