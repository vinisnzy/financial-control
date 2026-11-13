package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.responses.InstallmentExpenseResponse;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.expense.InstallmentExpenseService;
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

@WebMvcTest(InstallmentExpenseController.class)
class InstallmentExpenseControllerTest extends ControllerTest {

    @MockitoBean
    InstallmentExpenseService service;

    private UUID installmentId;
    private UUID categoryId;
    private UUID paymentMethodId;
    private InstallmentExpenseResponse response;

    @BeforeEach
    void setUp() {
        installmentId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
        response = new InstallmentExpenseResponse(installmentId, "Phone", new BigDecimal("3000.00"), (short) 10,
                categoryId, paymentMethodId, LocalDate.of(2026, 1, 10), List.of(), Instant.now(), null);
    }

    @Test
    void getAll_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getAll(eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/installment-expenses").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Phone"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(installmentId, authUser.getId())).thenReturn(response);

        mvc.perform(get("/installment-expenses/{id}", installmentId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(installmentId.toString()))
                .andExpect(jsonPath("$.installments").value(10));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(installmentId, authUser.getId()))
                .thenThrow(NotFoundException.of("Installment expense", installmentId));

        mvc.perform(get("/installment-expenses/{id}", installmentId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/installment-expenses/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/installment-expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Phone", "totalAmount": 3000.00, "installments": 10,
                                 "expenseCategoryId": "%s", "paymentMethodId": "%s", "firstDueDate": "2026-01-10"}
                                """.formatted(categoryId, paymentMethodId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/installment-expenses")
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
                "expenseCategoryId": "%s", "paymentMethodId": "%s", "firstDueDate": "2026-01-10"
                """.formatted(category, payment);
        return Stream.of(
                Arguments.of("blank name", "{\"name\": \"\", \"totalAmount\": 100, \"installments\": 2, " + ids + "}", "name"),
                Arguments.of("name above 100 characters", "{\"name\": \"" + "a".repeat(101) + "\", \"totalAmount\": 100, \"installments\": 2, " + ids + "}", "name"),
                Arguments.of("missing totalAmount", "{\"name\": \"Phone\", \"installments\": 2, " + ids + "}", "totalAmount"),
                Arguments.of("zero totalAmount", "{\"name\": \"Phone\", \"totalAmount\": 0, \"installments\": 2, " + ids + "}", "totalAmount"),
                Arguments.of("three decimal places", "{\"name\": \"Phone\", \"totalAmount\": 1.999, \"installments\": 2, " + ids + "}", "totalAmount"),
                Arguments.of("missing installments", "{\"name\": \"Phone\", \"totalAmount\": 100, " + ids + "}", "installments"),
                Arguments.of("installments below 2", "{\"name\": \"Phone\", \"totalAmount\": 100, \"installments\": 1, " + ids + "}", "installments"),
                Arguments.of("installments above 360", "{\"name\": \"Phone\", \"totalAmount\": 100, \"installments\": 361, " + ids + "}", "installments"),
                Arguments.of("missing expenseCategoryId", """
                        {"name": "Phone", "totalAmount": 100, "installments": 2, "paymentMethodId": "%s", "firstDueDate": "2026-01-10"}""".formatted(payment), "expenseCategoryId"),
                Arguments.of("missing paymentMethodId", """
                        {"name": "Phone", "totalAmount": 100, "installments": 2, "expenseCategoryId": "%s", "firstDueDate": "2026-01-10"}""".formatted(category), "paymentMethodId"),
                Arguments.of("missing firstDueDate", """
                        {"name": "Phone", "totalAmount": 100, "installments": 2, "expenseCategoryId": "%s", "paymentMethodId": "%s"}""".formatted(category, payment), "firstDueDate")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/installment-expenses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(installmentId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/installment-expenses/{id}", installmentId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Laptop"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(installmentId.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/installment-expenses/{id}", installmentId)
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
                Arguments.of("zero totalAmount", """
                        {"totalAmount": 0}""", "totalAmount"),
                Arguments.of("installments below 2", """
                        {"installments": 1}""", "installments"),
                Arguments.of("installments above 360", """
                        {"installments": 361}""", "installments")
        );
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/installment-expenses/{id}", installmentId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(installmentId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/installment-expenses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
