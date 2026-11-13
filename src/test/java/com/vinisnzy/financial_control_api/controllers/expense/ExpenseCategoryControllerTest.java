package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseCategoryResponse;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.expense.ExpenseCategoryService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseCategoryController.class)
class ExpenseCategoryControllerTest extends ControllerTest {

    @MockitoBean
    ExpenseCategoryService service;

    private UUID categoryId;
    private ExpenseCategoryResponse response;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        response = new ExpenseCategoryResponse(categoryId, "Food", "#FF5733", Instant.now(), null);
    }

    @Test
    void getAll_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getAll(eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/expense-categories").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(categoryId, authUser.getId())).thenReturn(response);

        mvc.perform(get("/expense-categories/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(categoryId, authUser.getId())).thenThrow(NotFoundException.of("Expense category", categoryId));

        mvc.perform(get("/expense-categories/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/expense-categories/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/expense-categories")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Food", "color": "#FF5733"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.color").value("#FF5733"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/expense-categories")
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
                        {"name": "", "color": "#FF5733"}""", "name"),
                Arguments.of("missing name", """
                        {"color": "#FF5733"}""", "name"),
                Arguments.of("name above 100 characters", """
                        {"name": "%s", "color": "#FF5733"}""".formatted("a".repeat(101)), "name"),
                Arguments.of("blank color", """
                        {"name": "Food", "color": ""}""", "color"),
                Arguments.of("missing color", """
                        {"name": "Food"}""", "color"),
                Arguments.of("color without hash", """
                        {"name": "Food", "color": "FF5733"}""", "color"),
                Arguments.of("color with invalid characters", """
                        {"name": "Food", "color": "#GGGGGG"}""", "color"),
                Arguments.of("color with wrong length", """
                        {"name": "Food", "color": "#FFF"}""", "color")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/expense-categories")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(categoryId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/expense-categories/{id}", categoryId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Transport"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/expense-categories/{id}", categoryId)
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
                Arguments.of("invalid color", """
                        {"color": "red"}""", "color")
        );
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/expense-categories/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(categoryId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/expense-categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
