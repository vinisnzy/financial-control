package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.FixedExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.expense.FixedExpenseMapper;
import com.vinisnzy.financial_control_api.repositories.expense.FixedExpenseRepository;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import com.vinisnzy.financial_control_api.services.billing.PaymentMethodService;
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
import java.time.LocalDate;
import java.time.YearMonth;
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
class FixedExpenseServiceTest {

    @Mock
    FixedExpenseRepository repository;
    @Mock
    FixedExpenseMapper mapper;
    @Mock
    UserService userService;
    @Mock
    ExpenseCategoryService expenseCategoryService;
    @Mock
    PaymentMethodService paymentMethodService;
    @Mock
    ExpenseService expenseService;

    @InjectMocks
    FixedExpenseService service;

    private UUID userId;
    private UUID fixedExpenseId;
    private UUID categoryId;
    private UUID paymentMethodId;
    private User user;
    private ExpenseCategory category;
    private PaymentMethod paymentMethod;
    private FixedExpense entity;
    private FixedExpenseResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        fixedExpenseId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        category = new ExpenseCategory();
        ReflectionTestUtils.setField(category, "id", categoryId);

        paymentMethod = new PaymentMethod();
        ReflectionTestUtils.setField(paymentMethod, "id", paymentMethodId);

        entity = new FixedExpense();
        ReflectionTestUtils.setField(entity, "id", fixedExpenseId);
        entity.setName("Rent");
        entity.setAmount(new BigDecimal("1500.00"));
        entity.setDueDay((short) 10);
        entity.setStartDate(LocalDate.of(2026, 1, 1));
        entity.setUser(user);
        entity.setExpenseCategory(category);
        entity.setPaymentMethod(paymentMethod);

        response = new FixedExpenseResponse(fixedExpenseId, "Rent", new BigDecimal("1500.00"), categoryId,
                paymentMethodId, (short) 10, LocalDate.of(2026, 1, 1), null, Instant.now(), null);
    }

    @Test
    void getEntityOwnerById_shouldReturnEntity() {
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.of(entity));

        assertThat(service.getEntityOwnerById(fixedExpenseId, userId)).isSameAs(entity);
    }

    @Test
    void getEntityOwnerById_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityOwnerById(fixedExpenseId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(fixedExpenseId.toString());
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
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(fixedExpenseId, userId)).isEqualTo(response);
    }

    @Test
    void getByExpenseCategory_shouldMapPage() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findByExpenseCategoryIdAndUserId(categoryId, userId, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByExpenseCategory(categoryId, userId, pageable).content()).containsExactly(response);
    }

    @Test
    void create_shouldSetRelationsAndSave() {
        var request = new CreateFixedExpenseRequest("Rent", new BigDecimal("1500.00"), categoryId, paymentMethodId,
                (short) 10, LocalDate.of(2026, 1, 1), null);
        FixedExpense newEntity = new FixedExpense();
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(paymentMethodId, userId)).thenReturn(paymentMethod);
        when(mapper.toResponse(newEntity)).thenReturn(response);

        assertThat(service.create(request, userId)).isEqualTo(response);

        ArgumentCaptor<FixedExpense> captor = ArgumentCaptor.forClass(FixedExpense.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getExpenseCategory()).isEqualTo(category);
        assertThat(captor.getValue().getPaymentMethod()).isEqualTo(paymentMethod);
    }

    @Test
    void create_whenPaymentMethodNotOwned_shouldThrowAndNotSave() {
        var request = new CreateFixedExpenseRequest("Rent", BigDecimal.TEN, categoryId, paymentMethodId,
                (short) 10, LocalDate.of(2026, 1, 1), null);
        when(mapper.toEntity(request)).thenReturn(new FixedExpense());
        when(userService.getEntity(userId)).thenReturn(user);
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(paymentMethodId, userId))
                .thenThrow(NotFoundException.of("Payment method", paymentMethodId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void generateForMonth_shouldCountOnlyCreatedExpenses() {
        YearMonth month = YearMonth.of(2026, 3);
        FixedExpense first = new FixedExpense();
        FixedExpense second = new FixedExpense();
        FixedExpense third = new FixedExpense();
        when(repository.findActiveBetween(month.atDay(1), month.atEndOfMonth())).thenReturn(List.of(first, second, third));
        when(expenseService.createByFixedExpenseAndMonth(first, month)).thenReturn(true);
        when(expenseService.createByFixedExpenseAndMonth(second, month)).thenReturn(false);
        when(expenseService.createByFixedExpenseAndMonth(third, month)).thenReturn(true);

        assertThat(service.generateForMonth(month)).isEqualTo(2);
    }

    @Test
    void generateForMonth_whenOneFails_shouldContinueWithOthers() {
        YearMonth month = YearMonth.of(2026, 3);
        FixedExpense failing = new FixedExpense();
        FixedExpense ok = new FixedExpense();
        when(repository.findActiveBetween(month.atDay(1), month.atEndOfMonth())).thenReturn(List.of(failing, ok));
        when(expenseService.createByFixedExpenseAndMonth(failing, month)).thenThrow(new RuntimeException("boom"));
        when(expenseService.createByFixedExpenseAndMonth(ok, month)).thenReturn(true);

        assertThat(service.generateForMonth(month)).isEqualTo(1);

        verify(expenseService).createByFixedExpenseAndMonth(ok, month);
    }

    @Test
    void generateForMonth_withoutActiveFixedExpenses_shouldReturnZero() {
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.findActiveBetween(month.atDay(1), month.atEndOfMonth())).thenReturn(List.of());

        assertThat(service.generateForMonth(month)).isZero();

        verifyNoInteractions(expenseService);
    }

    @Test
    void update_withRelations_shouldChangeCategoryAndPaymentMethod() {
        ExpenseCategory newCategory = new ExpenseCategory();
        PaymentMethod newPaymentMethod = new PaymentMethod();
        UUID newCategoryId = UUID.randomUUID();
        UUID newPaymentMethodId = UUID.randomUUID();
        var request = new UpdateFixedExpenseRequest(null, null, newCategoryId, newPaymentMethodId, null, null, null);
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.of(entity));
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(newCategoryId, userId)).thenReturn(newCategory);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(newPaymentMethodId, userId)).thenReturn(newPaymentMethod);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(fixedExpenseId, request, userId)).isEqualTo(response);

        assertThat(entity.getExpenseCategory()).isSameAs(newCategory);
        assertThat(entity.getPaymentMethod()).isSameAs(newPaymentMethod);
        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void update_withoutRelations_shouldKeepCategoryAndPaymentMethod() {
        var request = new UpdateFixedExpenseRequest("Other", null, null, null, (short) 5, null, null);
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        service.update(fixedExpenseId, request, userId);

        assertThat(entity.getExpenseCategory()).isSameAs(category);
        assertThat(entity.getPaymentMethod()).isSameAs(paymentMethod);
        verifyNoInteractions(expenseCategoryService, paymentMethodService);
    }

    @Test
    void update_whenNotOwned_shouldThrowAndNotSave() {
        var request = new UpdateFixedExpenseRequest("Other", null, null, null, null, null, null);
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(fixedExpenseId, request, userId))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteOwnedEntity() {
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.of(entity));

        service.delete(fixedExpenseId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotOwned_shouldNotDelete() {
        when(repository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(fixedExpenseId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
