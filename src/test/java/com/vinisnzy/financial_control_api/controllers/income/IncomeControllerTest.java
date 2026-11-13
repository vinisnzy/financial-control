package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeResponse;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.income.IncomeService;
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

@WebMvcTest(IncomeController.class)
class IncomeControllerTest extends ControllerTest {

    @MockitoBean
    IncomeService service;

    private UUID incomeId;
    private UUID categoryId;
    private UUID accountId;
    private IncomeResponse response;
    private PageResponse<IncomeResponse> page;

    @BeforeEach
    void setUp() {
        incomeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        response = new IncomeResponse(incomeId, "Salary", new BigDecimal("5000.00"), categoryId, accountId,
                LocalDate.of(2026, 1, 5), null, null, Instant.now(), null);
        page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
    }

    @Test
    void getAll_withoutPeriod_shouldReturn200WithPage() throws Exception {
        when(service.getAll(isNull(), isNull(), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/incomes").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Salary"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAll_withPeriod_shouldPassDatesToService() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(service.getAll(eq(start), eq(end), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/incomes").param("start", "2026-01-01").param("end", "2026-01-31").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(incomeId.toString()));
    }

    @Test
    void getAll_withInvalidPeriod_shouldReturn400() throws Exception {
        when(service.getAll(any(), any(), eq(authUser.getId()), any()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_PERIOD, "Start date must be before end date."));

        mvc.perform(get("/incomes").param("start", "2026-02-01").param("end", "2026-01-01").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }

    @Test
    void getAll_withMalformedDate_shouldReturn400() throws Exception {
        mvc.perform(get("/incomes").param("start", "31-01-2026").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(incomeId, authUser.getId())).thenReturn(response);

        mvc.perform(get("/incomes/{id}", incomeId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incomeId.toString()));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(incomeId, authUser.getId())).thenThrow(NotFoundException.of("Income", incomeId));

        mvc.perform(get("/incomes/{id}", incomeId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/incomes/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByName_shouldReturn200WithPage() throws Exception {
        when(service.getByName(eq("sal"), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/incomes/search").param("name", "sal").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Salary"));
    }

    @Test
    void getByName_withoutNameParam_shouldReturn400() throws Exception {
        mvc.perform(get("/incomes/search").with(user(authUser)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getByBankAccount_shouldReturn200WithPage() throws Exception {
        when(service.getByBankAccount(eq(accountId), isNull(), isNull(), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/incomes/bank-account/{id}", accountId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bankAccountId").value(accountId.toString()));
    }

    @Test
    void getByBankAccount_withPeriod_shouldPassDatesToService() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(service.getByBankAccount(eq(accountId), eq(start), eq(end), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/incomes/bank-account/{id}", accountId)
                        .param("start", "2026-01-01").param("end", "2026-01-31").with(user(authUser)))
                .andExpect(status().isOk());
    }

    @Test
    void getByBankAccount_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/incomes/bank-account/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByIncomeCategory_shouldReturn200WithPage() throws Exception {
        when(service.getByIncomeCategory(eq(categoryId), isNull(), isNull(), eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/incomes/income-category/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].incomeCategoryId").value(categoryId.toString()));
    }

    @Test
    void getByIncomeCategory_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/incomes/income-category/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(eq(authUser.getId()), any())).thenReturn(response);

        mvc.perform(post("/incomes")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Salary", "amount": 5000.00, "incomeCategoryId": "%s",
                                 "bankAccountId": "%s", "expectedAt": "2026-01-05"}
                                """.formatted(categoryId, accountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Salary"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/incomes")
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
        String ids = """
                "incomeCategoryId": "%s", "bankAccountId": "%s", "expectedAt": "2026-01-05"
                """.formatted(category, account);
        String future = LocalDate.now().plusDays(5).toString();
        return Stream.of(
                Arguments.of("blank name", "{\"name\": \"\", \"amount\": 10, " + ids + "}", "name"),
                Arguments.of("name above 100 characters", "{\"name\": \"" + "a".repeat(101) + "\", \"amount\": 10, " + ids + "}", "name"),
                Arguments.of("missing amount", "{\"name\": \"Salary\", " + ids + "}", "amount"),
                Arguments.of("zero amount", "{\"name\": \"Salary\", \"amount\": 0, " + ids + "}", "amount"),
                Arguments.of("three decimal places", "{\"name\": \"Salary\", \"amount\": 1.999, " + ids + "}", "amount"),
                Arguments.of("missing incomeCategoryId", """
                        {"name": "Salary", "amount": 10, "bankAccountId": "%s", "expectedAt": "2026-01-05"}""".formatted(account), "incomeCategoryId"),
                Arguments.of("missing bankAccountId", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "expectedAt": "2026-01-05"}""".formatted(category), "bankAccountId"),
                Arguments.of("missing expectedAt", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "bankAccountId": "%s"}""".formatted(category, account), "expectedAt"),
                Arguments.of("receivedAt in the future", """
                        {"name": "Salary", "amount": 10, "incomeCategoryId": "%s", "bankAccountId": "%s", "expectedAt": "2026-01-05", "receivedAt": "%s"}""".formatted(category, account, future), "receivedAt")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/incomes")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(incomeId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/incomes/{id}", incomeId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Bonus"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incomeId.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/incomes/{id}", incomeId)
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
                Arguments.of("receivedAt in the future", """
                        {"receivedAt": "%s"}""".formatted(LocalDate.now().plusDays(5)), "receivedAt")
        );
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/incomes/{id}", incomeId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(incomeId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/incomes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
