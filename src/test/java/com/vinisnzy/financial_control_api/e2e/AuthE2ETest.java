package com.vinisnzy.financial_control_api.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthE2ETest extends E2ETest {

    @Test
    void authCycle() throws Exception {
        String email = "mail-" + UUID.randomUUID() + "@test.com";

        signUp(email, PASSWORD).andExpect(status().isCreated());

        String response = signIn(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String accessToken = JsonPath.read(response, "$.accessToken");
        String refreshToken = JsonPath.read(response, "$.refreshToken");

        authed(get("/users/me"), accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        response = refresh(refreshToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String refreshedRefreshToken = JsonPath.read(response, "$.refreshToken");

        assertThat(refreshedRefreshToken).isNotEqualTo(refreshToken);

        logout(refreshedRefreshToken).andExpect(status().isNoContent());

        refresh(refreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));

        refresh(refreshedRefreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @Test
    void signUp_withDuplicatedEmail_shouldReturn409() throws Exception {
        Session session = signUpAndSignIn();

        signUp(session.email(), PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_WITH_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void signIn_withWrongPassword_shouldReturn401() throws Exception {
        Session session = signUpAndSignIn();

        signIn(session.email(), "wrongPassword")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @Test
    void refresh_afterLogout_shouldReturn401() throws Exception {
        Session session = signUpAndSignIn();

        logout(session.refreshToken()).andExpect(status().isNoContent());

        refresh(session.refreshToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }
}
