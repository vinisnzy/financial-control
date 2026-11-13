package com.vinisnzy.financial_control_api.controllers.billing;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.PaymentMethodResponse;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.billing.PaymentMethodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentMethodController.class)
class PaymentMethodControllerTest extends ControllerTest {

    @MockitoBean
    PaymentMethodService service;

    private UUID paymentMethodId;
    private UUID accountId;
    private PaymentMethodResponse response;

    @BeforeEach
    void setUp() {
        paymentMethodId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        response = new PaymentMethodResponse(paymentMethodId,
                "Nubank credit",
                "Credit card",
                accountId,
                Instant.now(),
                null
        );
    }

    @Test
    void getAll_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getAll(eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/payment-methods").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Nubank credit"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(paymentMethodId, authUser.getId())).thenReturn(response);
        mvc.perform(get("/payment-methods/" + paymentMethodId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentMethodId.toString()));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(paymentMethodId, authUser.getId()))
                .thenThrow(NotFoundException.of("Payment Method", paymentMethodId));
        mvc.perform(get("/payment-methods/" + paymentMethodId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/payment-methods/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void getByBankAccountId_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getByBankAccountId(eq(authUser.getId()), eq(accountId), any())).thenReturn(page);
        mvc.perform(get("/payment-methods/bank-account/" + accountId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bankAccountId").value(accountId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getByBankAccountId_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/payment-methods/bank-account/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/payment-methods")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Nubank credit", "type": "Credit card", "bankAccountId": "%s"}
                                """.formatted(accountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nubank credit"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/payment-methods")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem(field)));

        verifyNoInteractions(service);
    }

    static Stream<Arguments> invalidCreateBodies() {
        return Stream.of(
                Arguments.of("blank name", """
                        {"name": "", "type": "Credit card", "bankAccountId": "%s"}""".formatted(UUID.randomUUID()), "name"),
                Arguments.of("missing name", """
                        {"type": "Credit card", "bankAccountId": "%s"}""".formatted(UUID.randomUUID()), "name"),
                Arguments.of("name above 100 characters", """
                        {"name": "%s", "type": "Credit card", "bankAccountId": "%s"}""".formatted("a".repeat(101), UUID.randomUUID()), "name"),
                Arguments.of("blank type", """
                        {"name": "Nubank credit", "type": "", "bankAccountId": "%s"}""".formatted(UUID.randomUUID()), "type"),
                Arguments.of("missing type", """
                        {"name": "Nubank credit", "bankAccountId": "%s"}""".formatted(UUID.randomUUID()), "type"),
                Arguments.of("type above 100 characters", """
                        {"name": "Nubank credit", "type": "%s", "bankAccountId": "%s"}""".formatted("a".repeat(101), UUID.randomUUID()), "type"),
                Arguments.of("missing bankAccountId", """
                        {"name": "Nubank credit", "type": "Credit card"}""", "bankAccountId")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/payment-methods")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(paymentMethodId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/payment-methods/" + paymentMethodId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Inter"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void update_withBlankName_shouldReturn400() throws Exception {
        mvc.perform(patch("/payment-methods/" + paymentMethodId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")));

        verifyNoInteractions(service);
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/payment-methods/" + paymentMethodId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(paymentMethodId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/payment-methods"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}