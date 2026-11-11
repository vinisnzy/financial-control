package com.vinisnzy.financial_control_api.services.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.PaymentMethodResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.billing.PaymentMethodMapper;
import com.vinisnzy.financial_control_api.repositories.billing.PaymentMethodRepository;
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
class PaymentMethodServiceTest {

    @Mock
    PaymentMethodRepository repository;
    @Mock
    PaymentMethodMapper mapper;
    @Mock
    BankAccountService bankAccountService;
    @Mock
    UserService userService;

    @InjectMocks
    PaymentMethodService service;

    private UUID userId;
    private UUID paymentMethodId;
    private UUID bankAccountId;
    private User user;
    private BankAccount bankAccount;
    private PaymentMethod entity;
    private PaymentMethodResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
        bankAccountId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        bankAccount = new BankAccount();
        ReflectionTestUtils.setField(bankAccount, "id", bankAccountId);

        entity = new PaymentMethod();
        ReflectionTestUtils.setField(entity, "id", paymentMethodId);
        entity.setName("Nubank Credit");
        entity.setType("CREDIT");
        entity.setUser(user);
        entity.setBankAccount(bankAccount);

        response = new PaymentMethodResponse(paymentMethodId, "Nubank Credit", "CREDIT", bankAccountId, Instant.now(), null);
    }

    @Test
    void getEntityOwnedByIdOrThrow_shouldReturnEntity() {
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.of(entity));

        assertThat(service.getEntityOwnedByIdOrThrow(paymentMethodId, userId)).isSameAs(entity);
    }

    @Test
    void getEntityOwnedByIdOrThrow_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityOwnedByIdOrThrow(paymentMethodId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(paymentMethodId.toString());
    }

    @Test
    void getAll_shouldMapPage() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findByUserId(userId, pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.getAll(userId, pageable);

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void getById_shouldReturnMappedEntity() {
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(paymentMethodId, userId)).isEqualTo(response);
    }

    @Test
    void getById_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(paymentMethodId, userId)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(mapper);
    }

    @Test
    void getByBankAccountId_shouldMapPage() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findByUserIdAndBankAccountId(userId, bankAccountId, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.getByBankAccountId(userId, bankAccountId, pageable);

        assertThat(result.content()).containsExactly(response);
    }

    @Test
    void create_shouldSetUserAndBankAccountAndSave() {
        var request = new CreatePaymentMethodRequest("Nubank Credit", "CREDIT", bankAccountId);
        PaymentMethod newEntity = new PaymentMethod();
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(bankAccountService.getEntityOwnedByIdOrThrow(bankAccountId, userId)).thenReturn(bankAccount);
        when(mapper.toResponse(newEntity)).thenReturn(response);

        assertThat(service.create(request, userId)).isEqualTo(response);

        ArgumentCaptor<PaymentMethod> captor = ArgumentCaptor.forClass(PaymentMethod.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getBankAccount()).isEqualTo(bankAccount);
    }

    @Test
    void create_whenBankAccountNotOwned_shouldThrowAndNotSave() {
        var request = new CreatePaymentMethodRequest("Nubank Credit", "CREDIT", bankAccountId);
        when(mapper.toEntity(request)).thenReturn(new PaymentMethod());
        when(userService.getEntity(userId)).thenReturn(user);
        when(bankAccountService.getEntityOwnedByIdOrThrow(bankAccountId, userId))
                .thenThrow(NotFoundException.of("Bank", bankAccountId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_whenUserNotFound_shouldThrowAndNotSave() {
        var request = new CreatePaymentMethodRequest("Nubank Credit", "CREDIT", bankAccountId);
        when(mapper.toEntity(request)).thenReturn(new PaymentMethod());
        when(userService.getEntity(userId)).thenThrow(NotFoundException.of("User", userId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(bankAccountService);
        verify(repository, never()).save(any());
    }

    @Test
    void update_withBankAccountId_shouldChangeBankAccount() {
        UUID newBankAccountId = UUID.randomUUID();
        BankAccount newBankAccount = new BankAccount();
        ReflectionTestUtils.setField(newBankAccount, "id", newBankAccountId);
        var request = new UpdatePaymentMethodRequest(null, null, newBankAccountId);
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.of(entity));
        when(bankAccountService.getEntityOwnedByIdOrThrow(newBankAccountId, userId)).thenReturn(newBankAccount);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(paymentMethodId, request, userId)).isEqualTo(response);

        assertThat(entity.getBankAccount()).isSameAs(newBankAccount);
        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void update_withoutBankAccountId_shouldKeepBankAccount() {
        var request = new UpdatePaymentMethodRequest("Other", null, null);
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        service.update(paymentMethodId, request, userId);

        assertThat(entity.getBankAccount()).isSameAs(bankAccount);
        verifyNoInteractions(bankAccountService);
        verify(repository).save(entity);
    }

    @Test
    void update_whenNewBankAccountNotOwned_shouldThrowAndNotSave() {
        UUID otherBankAccountId = UUID.randomUUID();
        var request = new UpdatePaymentMethodRequest(null, null, otherBankAccountId);
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.of(entity));
        when(bankAccountService.getEntityOwnedByIdOrThrow(otherBankAccountId, userId))
                .thenThrow(NotFoundException.of("Bank", otherBankAccountId));

        assertThatThrownBy(() -> service.update(paymentMethodId, request, userId))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenNotOwned_shouldThrowAndNotSave() {
        var request = new UpdatePaymentMethodRequest("Other", null, null);
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(paymentMethodId, request, userId))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteOwnedEntity() {
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.of(entity));

        service.delete(paymentMethodId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotOwned_shouldNotDelete() {
        when(repository.findByIdAndUserId(paymentMethodId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(paymentMethodId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
