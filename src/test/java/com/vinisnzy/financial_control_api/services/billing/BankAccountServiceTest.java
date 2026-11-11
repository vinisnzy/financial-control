package com.vinisnzy.financial_control_api.services.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.BankAccountResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.billing.BankAccountMapper;
import com.vinisnzy.financial_control_api.repositories.billing.BankAccountRepository;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
class BankAccountServiceTest {

    @Mock
    BankAccountRepository repository;
    @Mock
    BankAccountMapper mapper;
    @Mock
    UserService userService;

    @InjectMocks
    BankAccountService service;

    private UUID userId;
    private UUID accountId;
    private User user;
    private BankAccount entity;
    private BankAccountResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        entity = new BankAccount();
        ReflectionTestUtils.setField(entity, "id", accountId);
        entity.setName("Nubank");
        entity.setInitialBalance(new BigDecimal("100.00"));
        entity.setUser(user);

        response = new BankAccountResponse(accountId, "Nubank", new BigDecimal("100.00"), Instant.now(), null);
    }

    @Test
    void getEntityOwnedByIdOrThrow_shouldReturnEntity() {
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(entity));

        assertThat(service.getEntityOwnedByIdOrThrow(accountId, userId)).isSameAs(entity);
    }

    @Test
    void getEntityOwnedByIdOrThrow_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityOwnedByIdOrThrow(accountId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(accountId.toString());
    }

    @Test
    void getAll_shouldMapPage() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findByUserId(userId, pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.getAll(userId, pageable);

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
    }

    @Test
    void getById_shouldReturnMappedEntity() {
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(accountId, userId)).isEqualTo(response);
    }

    @Test
    void getById_whenNotOwned_shouldThrowNotFound() {
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(accountId, userId)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(mapper);
    }

    @Test
    void create_shouldSetUserAndSave() {
        var request = new CreateBankAccountRequest("Nubank", new BigDecimal("100.00"));
        BankAccount newEntity = new BankAccount();
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(mapper.toResponse(newEntity)).thenReturn(response);

        assertThat(service.create(request, userId)).isEqualTo(response);

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(newEntity);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void create_whenUserNotFound_shouldThrowAndNotSave() {
        var request = new CreateBankAccountRequest("Nubank", BigDecimal.ZERO);
        when(mapper.toEntity(request)).thenReturn(new BankAccount());
        when(userService.getEntity(userId)).thenThrow(NotFoundException.of("User", userId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldApplyChangesAndSave() {
        var request = new UpdateBankAccountRequest("Inter", null);
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(accountId, request, userId)).isEqualTo(response);

        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void update_whenNotOwned_shouldThrowAndNotSave() {
        var request = new UpdateBankAccountRequest("Inter", null);
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(accountId, request, userId)).isInstanceOf(NotFoundException.class);

        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteOwnedEntity() {
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(entity));

        service.delete(accountId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotOwned_shouldNotDelete() {
        when(repository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(accountId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
