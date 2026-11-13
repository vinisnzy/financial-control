package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.responses.FixedIncomeResponse;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.income.FixedIncomeService;
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

@WebMvcTest(FixedIncomeController.class)
class FixedIncomeControllerTest extends ControllerTest {

    @MockitoBean
    FixedIncomeService service;

    private UUID fixedIncomeId;
    private UUID categoryId;
    private UUID accountId;
    private FixedIncomeResponse response;

    @BeforeEach
    void setUp() {
        fixedIncomeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        response = new FixedIncomeResponse(fixedIncomeId, "Salary", new BigDecimal("5000.00"), categoryId,
                accountId, (short) 5, LocalDate.of(2026, 1, 1), null, Instant.now(), null);
    }

    @Test
    void getAll_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getAll(eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/fixed-incomes").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Salary"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(fixedIncomeId, authUser.getId())).thenReturn(response);

        mvc.perform(get("/fixed-incomes/{id}", fixedIncomeId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fixedIncomeId.toString()));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(fixedIncomeId, authUser.getId()))
                .thenThrow(NotFoundException.of("Fixed income", fixedIncomeId));

        mvc.perform(get("/fixed-incomes/{id}", fixedIncomeId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/fixed-incomes/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/fixed-incomes")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Salary", "amount": 5000.00, "incomeCategoryId": "%s", "bankAccountId": "%s",
                                 "payday": 5, "startDate": "2026-01-01"}
                                """.formatted(categoryId, accountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Salary"))
                .andExpect(jsonPath("$.payday").value(5));
    }

    @Test
    void create_withEndDateEqualToStartDate_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/fixed-incomes")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Salary", "amount": 5000.00, "incomeCategoryId": "%s", "bankAccountId": "%s",
                                 "payday": 5, "startDate": "2026-01-01", "endDate": "2026-01-01"}
                                """.formatted(categoryId, accountId)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/fixed-incomes")
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
        String account = UUID.randomUUID().toString();
        String base = """
                "incomeCategoryId": "%s", "bankAccountId": "%s", "payday": 5, "startDate": "2026-01-01"
                """.formatted(category, account);
        return Stream.of(
                Arguments.of("blank name", "{\"name\": \"\", \"amount\": 10, " + base + "}", "name"),
                Arguments.of("name above 100 characters", "{\"name\": \"" + "a".repeat(101) + "\", \"amount\": 10, " + base + "}", "name"),
                Arguments.of("missing amount", "{\"name\": \"Salary\", " + base + "}", "amount"),
                Arguments.of("zero amount", "{\"name\": \"Salary\", \"amount\": 0, " + base + "}", "amount"),
                Arguments.of("three decimal places", "{\"name\": \"Salary\", \"amount\": 1.999, " + base + "}", "amount"),
                Arguments.of("missing incomeCategoryId", """
                        {"name": "Salary", "amount": 10, "bankAccountId": "%s", "payday": 5, "startDate": "2026-01-01"}""".formatted(account), "incomeCategoryId"),
                Arguments.of("missing bankAccountId", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "payday": 5, "startDate": "2026-01-01"}""".formatted(category), "bankAccountId"),
                Arguments.of("missing payday", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "bankAccountId": "%s", "startDate": "2026-01-01"}""".formatted(category, account), "payday"),
                Arguments.of("payday below 1", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "bankAccountId": "%s", "payday": 0, "startDate": "2026-01-01"}""".formatted(category, account), "payday"),
                Arguments.of("payday above 31", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "bankAccountId": "%s", "payday": 32, "startDate": "2026-01-01"}""".formatted(category, account), "payday"),
                Arguments.of("missing startDate", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "bankAccountId": "%s", "payday": 5}""".formatted(category, account), "startDate"),
                Arguments.of("endDate before startDate", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "bankAccountId": "%s", "payday": 5, "startDate": "2026-02-01", "endDate": "2026-01-01"}""".formatted(category, account), "endDateValid")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/fixed-incomes")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(fixedIncomeId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/fixed-incomes/{id}", fixedIncomeId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Bonus"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fixedIncomeId.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/fixed-incomes/{id}", fixedIncomeId)
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
                Arguments.of("payday above 31", """
                        {"payday": 32}""", "payday"),
                Arguments.of("endDate before startDate", """
                        {"startDate": "2026-02-01", "endDate": "2026-01-01"}""", "endDateValid")
        );
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/fixed-incomes/{id}", fixedIncomeId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(fixedIncomeId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/fixed-incomes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
