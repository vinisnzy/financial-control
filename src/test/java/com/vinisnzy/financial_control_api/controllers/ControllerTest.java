package com.vinisnzy.financial_control_api.controllers;

import com.vinisnzy.financial_control_api.config.AuthConfig;
import com.vinisnzy.financial_control_api.config.auth.RestAuthenticationEntryPoint;
import com.vinisnzy.financial_control_api.config.auth.TokenProvider;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.repositories.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@Import({AuthConfig.class, RestAuthenticationEntryPoint.class})
public abstract class ControllerTest {

    @Autowired
    protected MockMvc mvc;

    @MockitoBean
    protected TokenProvider tokenProvider;
    @MockitoBean
    protected UserRepository userRepository;

    protected User authUser;

    @BeforeEach
    void setUpAuthUser() {
        authUser = new User();
        authUser.setEmail("vini@test.com");
        ReflectionTestUtils.setField(authUser, "id", UUID.randomUUID());
    }
}
