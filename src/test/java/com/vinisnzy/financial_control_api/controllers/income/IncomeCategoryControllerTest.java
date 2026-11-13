package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeCategoryResponse;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.income.IncomeCategoryService;
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

@WebMvcTest(IncomeCategoryController.class)
class IncomeCategoryControllerTest extends ControllerTest {

    @MockitoBean
    IncomeCategoryService service;

    private UUID categoryId;
    private IncomeCategoryResponse response;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        response = new IncomeCategoryResponse(categoryId, "Salary", "#00AA55", Instant.now(), null);
    }

    @Test
    void getAll_shouldReturn200WithPage() throws Exception {
        var page = new PageResponse<>(List.of(response), 0, 10, 1L, 1, true, true);
        when(service.getAll(eq(authUser.getId()), any())).thenReturn(page);

        mvc.perform(get("/income-categories").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Salary"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getById(categoryId, authUser.getId())).thenReturn(response);

        mvc.perform(get("/income-categories/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(service.getById(categoryId, authUser.getId())).thenThrow(NotFoundException.of("Income category", categoryId));

        mvc.perform(get("/income-categories/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_withInvalidUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/income-categories/abc").with(user(authUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        verifyNoInteractions(service);
    }

    @Test
    void create_withValidBody_shouldReturn201() throws Exception {
        when(service.create(any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(post("/income-categories")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Salary", "color": "#00AA55"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Salary"))
                .andExpect(jsonPath("$.color").value("#00AA55"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void create_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/income-categories")
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
                        {"name": "", "color": "#00AA55"}""", "name"),
                Arguments.of("missing name", """
                        {"color": "#00AA55"}""", "name"),
                Arguments.of("name above 100 characters", """
                        {"name": "%s", "color": "#00AA55"}""".formatted("a".repeat(101)), "name"),
                Arguments.of("blank color", """
                        {"name": "Salary", "color": ""}""", "color"),
                Arguments.of("missing color", """
                        {"name": "Salary"}""", "color"),
                Arguments.of("color without hash", """
                        {"name": "Salary", "color": "00AA55"}""", "color"),
                Arguments.of("color with invalid characters", """
                        {"name": "Salary", "color": "#GGGGGG"}""", "color"),
                Arguments.of("color with wrong length", """
                        {"name": "Salary", "color": "#FFF"}""", "color")
        );
    }

    @Test
    void create_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/income-categories")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_withPartialBody_shouldReturn200() throws Exception {
        when(service.update(eq(categoryId), any(), eq(authUser.getId()))).thenReturn(response);

        mvc.perform(patch("/income-categories/{id}", categoryId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Freelance"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/income-categories/{id}", categoryId)
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
        mvc.perform(delete("/income-categories/{id}", categoryId).with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(categoryId, authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/income-categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
