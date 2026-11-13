package com.vinisnzy.financial_control_api.controllers.auth;

import com.vinisnzy.financial_control_api.controllers.ControllerTest;
import com.vinisnzy.financial_control_api.dtos.auth.SignInResponse;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.services.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTest {

    @MockitoBean
    AuthService service;

    @Test
    void signUp_withValidBody_shouldReturn201WithoutAuthentication() throws Exception {
        mvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "vini@test.com", "password": "12345678"}
                                """))
                .andExpect(status().isCreated());

        verify(service).signUp(any());
    }

    @Test
    void signUp_whenEmailAlreadyExists_shouldReturn409() throws Exception {
        doThrow(new BusinessException(ErrorCode.USER_WITH_EMAIL_ALREADY_EXISTS, "User already exists"))
                .when(service).signUp(any());

        mvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "vini@test.com", "password": "12345678"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_WITH_EMAIL_ALREADY_EXISTS"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSignUpBodies")
    void signUp_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem(field)));

        verifyNoInteractions(service);
    }

    static Stream<Arguments> invalidSignUpBodies() {
        return Stream.of(
                Arguments.of("blank email", """
                        {"email": "", "password": "12345678"}""", "email"),
                Arguments.of("missing email", """
                        {"password": "12345678"}""", "email"),
                Arguments.of("malformed email", """
                        {"email": "not-an-email", "password": "12345678"}""", "email"),
                Arguments.of("blank password", """
                        {"email": "vini@test.com", "password": ""}""", "password"),
                Arguments.of("missing password", """
                        {"email": "vini@test.com"}""", "password"),
                Arguments.of("password below 8 characters", """
                        {"email": "vini@test.com", "password": "1234567"}""", "password")
        );
    }

    @Test
    void signIn_withValidBody_shouldReturn200WithTokens() throws Exception {
        when(service.signIn(any())).thenReturn(new SignInResponse("access", "refresh"));

        mvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "vini@test.com", "password": "12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void signIn_withWrongCredentials_shouldReturn401() throws Exception {
        when(service.signIn(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "vini@test.com", "password": "wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSignInBodies")
    void signIn_withInvalidBody_shouldReturn400(String scenario, String body, String field) throws Exception {
        mvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem(field)));

        verifyNoInteractions(service);
    }

    static Stream<Arguments> invalidSignInBodies() {
        return Stream.of(
                Arguments.of("blank email", """
                        {"email": "", "password": "12345678"}""", "email"),
                Arguments.of("missing email", """
                        {"password": "12345678"}""", "email"),
                Arguments.of("blank password", """
                        {"email": "vini@test.com", "password": ""}""", "password"),
                Arguments.of("missing password", """
                        {"email": "vini@test.com"}""", "password")
        );
    }

    @Test
    void refresh_withValidBody_shouldReturn200WithTokens() throws Exception {
        when(service.refresh(any())).thenReturn(new SignInResponse("new-access", "new-refresh"));

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "old-refresh"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRefreshTokenBodies")
    void refresh_withInvalidBody_shouldReturn400(String scenario, String body) throws Exception {
        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("refreshToken")));

        verifyNoInteractions(service);
    }

    @Test
    void logout_withValidBody_shouldReturn204() throws Exception {
        mvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "some-refresh"}
                                """))
                .andExpect(status().isNoContent());

        verify(service).logout(any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRefreshTokenBodies")
    void logout_withInvalidBody_shouldReturn400(String scenario, String body) throws Exception {
        mvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("refreshToken")));

        verifyNoInteractions(service);
    }

    static Stream<Arguments> invalidRefreshTokenBodies() {
        return Stream.of(
                Arguments.of("blank refreshToken", """
                        {"refreshToken": ""}"""),
                Arguments.of("missing refreshToken", "{}")
        );
    }

    @Test
    void signUp_withMalformedJson_shouldReturn400() throws Exception {
        mvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
