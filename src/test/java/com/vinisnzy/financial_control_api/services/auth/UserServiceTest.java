package com.vinisnzy.financial_control_api.services.auth;

import com.vinisnzy.financial_control_api.dtos.user.requests.UpdateUserRequest;
import com.vinisnzy.financial_control_api.dtos.user.responses.UserResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.UserMapper;
import com.vinisnzy.financial_control_api.repositories.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;
    @Mock
    UserMapper mapper;

    @InjectMocks
    UserService service;

    private UUID userId;
    private User user;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setEmail("vini@test.com");
        response = new UserResponse(userId, "vini@test.com", Instant.now(), null);
    }

    @Test
    void getEntity_shouldReturnUser() {
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        assertThat(service.getEntity(userId)).isSameAs(user);
    }

    @Test
    void getEntity_whenNotFound_shouldThrow() {
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntity(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void getById_shouldReturnMappedUser() {
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        assertThat(service.getById(userId)).isEqualTo(response);
    }

    @Test
    void getById_whenNotFound_shouldThrowAndNotMap() {
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(userId)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(mapper);
    }

    @Test
    void getByEmail_shouldNormalizeEmail() {
        when(repository.findByEmail("vini@test.com")).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        assertThat(service.getByEmail("  Vini@Test.COM ")).isEqualTo(response);
    }

    @Test
    void getByEmail_whenNotFound_shouldThrow() {
        when(repository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByEmail("x@test.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldApplyChangesAndSave() {
        var request = new UpdateUserRequest("new@test.com");
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        assertThat(service.update(userId, request)).isEqualTo(response);

        verify(mapper).updateEntity(request, user);
        verify(repository).save(user);
    }

    @Test
    void update_whenNotFound_shouldThrowAndNotSave() {
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(userId, new UpdateUserRequest("new@test.com")))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteById() {
        service.delete(userId);

        verify(repository).deleteById(userId);
    }
}
