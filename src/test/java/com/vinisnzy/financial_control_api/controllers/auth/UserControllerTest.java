package com.vinisnzy.financial_control_api.controllers.auth;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.user.responses.UserResponse;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest extends ControllerTest {

    @MockitoBean
    UserService service;

    private UserResponse response;

    @BeforeEach
    void setUp() {
        response = new UserResponse(authUser.getId(), "vini@test.com", Instant.now(), null);
    }

    @Test
    void getMe_shouldReturn200() throws Exception {
        when(service.getById(authUser.getId())).thenReturn(response);

        mvc.perform(get("/users/me").with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(authUser.getId().toString()))
                .andExpect(jsonPath("$.email").value("vini@test.com"));
    }

    @Test
    void getMe_whenUserNotFound_shouldReturn404() throws Exception {
        when(service.getById(authUser.getId())).thenThrow(NotFoundException.of("User", authUser.getId()));

        mvc.perform(get("/users/me").with(user(authUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void update_withValidBody_shouldReturn200() throws Exception {
        when(service.update(eq(authUser.getId()), any())).thenReturn(response);

        mvc.perform(patch("/users/me")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "new@test.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("vini@test.com"));
    }

    @Test
    void update_withEmptyBody_shouldReturn200() throws Exception {
        when(service.update(eq(authUser.getId()), any())).thenReturn(response);

        mvc.perform(patch("/users/me")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void update_whenEmailAlreadyExists_shouldReturn409() throws Exception {
        when(service.update(eq(authUser.getId()), any()))
                .thenThrow(new BusinessException(ErrorCode.USER_WITH_EMAIL_ALREADY_EXISTS, "User already exists"));

        mvc.perform(patch("/users/me")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "taken@test.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_WITH_EMAIL_ALREADY_EXISTS"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateBodies")
    void update_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(patch("/users/me")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem(field)));

        verifyNoInteractions(service);
    }

    static Stream<Arguments> invalidUpdateBodies() {
        return Stream.of(
                Arguments.of("malformed email", """
                        {"email": "not-an-email"}""", "email"),
                Arguments.of("email above 255 characters", """
                        {"email": "%s@test.com"}""".formatted("a".repeat(250)), "email")
        );
    }

    @Test
    void update_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(patch("/users/me")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/users/me").with(user(authUser)))
                .andExpect(status().isNoContent());

        verify(service).delete(authUser.getId());
    }

    @Test
    void anyRoute_withoutAuthentication_shouldReturn401() throws Exception {
        mvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verifyNoInteractions(service);
    }
}
