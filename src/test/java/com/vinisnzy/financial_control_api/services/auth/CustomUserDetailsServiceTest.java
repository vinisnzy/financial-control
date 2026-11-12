package com.vinisnzy.financial_control_api.services.auth;

import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.repositories.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CustomUserDetailsService service;

    @Test
    void loadUserByUsername_shouldReturnUser() {
        User user = new User();
        user.setEmail("vini@test.com");
        when(userRepository.findByEmail("vini@test.com")).thenReturn(Optional.of(user));

        assertThat(service.loadUserByUsername("vini@test.com")).isSameAs(user);
    }

    @Test
    void loadUserByUsername_whenNotFound_shouldThrow() {
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("x@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
