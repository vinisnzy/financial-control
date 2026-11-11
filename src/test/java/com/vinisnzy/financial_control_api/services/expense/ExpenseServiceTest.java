package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.expense.ExpenseMapper;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseRepository;
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
import org.springframework.data.domain.Pageable;
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
class ExpenseServiceTest {

    @Mock
    ExpenseRepository repository;
    @Mock
    ExpenseMapper mapper;
    @Mock
    UserService userService;
    @Mock
    ExpenseCategoryService expenseCategoryService;
    @Mock
    PaymentMethodService paymentMethodService;

    @InjectMocks
    ExpenseService service;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 1, 31);

    private UUID userId;
    private UUID expenseId;
    private UUID categoryId;
    private UUID paymentMethodId;
    private User user;
    private ExpenseCategory category;
    private PaymentMethod paymentMethod;
    private Expense entity;
    private ExpenseResponse response;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        category = new ExpenseCategory();
        ReflectionTestUtils.setField(category, "id", categoryId);

        paymentMethod = new PaymentMethod();
        ReflectionTestUtils.setField(paymentMethod, "id", paymentMethodId);

        entity = new Expense();
        ReflectionTestUtils.setField(entity, "id", expenseId);
        entity.setName("Market");
        entity.setAmount(new BigDecimal("50.00"));
        entity.setUser(user);
        entity.setExpenseCategory(category);
        entity.setPaymentMethod(paymentMethod);
        entity.setDueDate(START);

        response = new ExpenseResponse(expenseId, "Market", new BigDecimal("50.00"), categoryId, paymentMethodId,
                START, null, null, null, null, Instant.now(), null);
    }

    private PageImpl<Expense> page() {
        return new PageImpl<>(List.of(entity), pageable, 1);
    }

    @Test
    void getAll_withoutPeriod_shouldSearchByUser() {
        when(repository.findByUserId(userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.getAll(null, null, userId, pageable);

        assertThat(result.content()).containsExactly(response);
        verify(repository, never()).findByUserIdAndDueDateBetween(any(), any(), any(), any());
    }

    @Test
    void getAll_withPeriod_shouldSearchByDueDateBetween() {
        when(repository.findByUserIdAndDueDateBetween(userId, START, END, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        var result = service.getAll(START, END, userId, pageable);

        assertThat(result.content()).containsExactly(response);
        verify(repository, never()).findByUserId(any(), any());
    }

    @Test
    void getAll_withInvalidPeriod_shouldThrowAndNotQuery() {
        assertThatThrownBy(() -> service.getAll(START, null, userId, pageable))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void getById_shouldReturnMappedEntity() {
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(expenseId, userId)).isEqualTo(response);
    }

    @Test
    void getById_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(expenseId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(expenseId.toString());

        verifyNoInteractions(mapper);
    }

    @Test
    void getByName_shouldMapPage() {
        when(repository.findByNameContainingIgnoreCaseAndUserId("mar", userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByName("mar", userId, pageable).content()).containsExactly(response);
    }

    @Test
    void getByExpenseCategory_withoutPeriod_shouldSearchByCategory() {
        when(repository.findByExpenseCategoryIdAndUserId(categoryId, userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByExpenseCategory(categoryId, null, null, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByExpenseCategory_withPeriod_shouldSearchByCategoryAndDueDate() {
        when(repository.findByExpenseCategoryIdAndUserIdAndDueDateBetween(categoryId, userId, START, END, pageable))
                .thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByExpenseCategory(categoryId, START, END, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByExpenseCategory_withInvalidPeriod_shouldThrow() {
        assertThatThrownBy(() -> service.getByExpenseCategory(categoryId, END, START, userId, pageable))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void getByPaymentMethod_withoutPeriod_shouldSearchByPaymentMethod() {
        when(repository.findByPaymentMethodIdAndUserId(paymentMethodId, userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByPaymentMethod(paymentMethodId, null, null, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByPaymentMethod_withPeriod_shouldSearchByPaymentMethodAndDueDate() {
        when(repository.findByPaymentMethodIdAndUserIdAndDueDateBetween(paymentMethodId, userId, START, END, pageable))
                .thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByPaymentMethod(paymentMethodId, START, END, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByPaymentMethod_withInvalidPeriod_shouldThrow() {
        assertThatThrownBy(() -> service.getByPaymentMethod(paymentMethodId, null, END, userId, pageable))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void getByInstallmentExpense_shouldMapPage() {
        UUID installmentId = UUID.randomUUID();
        when(repository.findByInstallmentExpenseIdAndUserId(installmentId, userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByInstallmentExpense(installmentId, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void create_shouldSetRelationsAndSave() {
        var request = new CreateExpenseRequest("Market", new BigDecimal("50.00"), categoryId, paymentMethodId, START, null);
        Expense newEntity = new Expense();
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(paymentMethodId, userId)).thenReturn(paymentMethod);
        when(mapper.toResponse(newEntity)).thenReturn(response);

        assertThat(service.create(request, userId)).isEqualTo(response);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getExpenseCategory()).isEqualTo(category);
        assertThat(captor.getValue().getPaymentMethod()).isEqualTo(paymentMethod);
    }

    @Test
    void create_whenCategoryNotFound_shouldThrowAndNotSave() {
        var request = new CreateExpenseRequest("Market", BigDecimal.TEN, categoryId, paymentMethodId, START, null);
        when(mapper.toEntity(request)).thenReturn(new Expense());
        when(userService.getEntity(userId)).thenReturn(user);
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId))
                .thenThrow(NotFoundException.of("Expense category", categoryId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(paymentMethodService);
        verify(repository, never()).save(any());
    }

    @Test
    void create_whenPaymentMethodNotOwned_shouldThrowAndNotSave() {
        var request = new CreateExpenseRequest("Market", BigDecimal.TEN, categoryId, paymentMethodId, START, null);
        when(mapper.toEntity(request)).thenReturn(new Expense());
        when(userService.getEntity(userId)).thenReturn(user);
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(paymentMethodId, userId))
                .thenThrow(NotFoundException.of("Payment method", paymentMethodId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void createByFixedExpenseAndMonth_whenAlreadyGenerated_shouldReturnFalse() {
        FixedExpense fixed = fixedExpense((short) 10);
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.existsByFixedExpenseIdAndDueDateBetween(fixed.getId(), month.atDay(1), month.atEndOfMonth()))
                .thenReturn(true);

        assertThat(service.createByFixedExpenseAndMonth(fixed, month)).isFalse();

        verify(repository, never()).save(any());
    }

    @Test
    void createByFixedExpenseAndMonth_shouldCopyFixedExpenseData() {
        FixedExpense fixed = fixedExpense((short) 10);
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.existsByFixedExpenseIdAndDueDateBetween(fixed.getId(), month.atDay(1), month.atEndOfMonth()))
                .thenReturn(false);

        assertThat(service.createByFixedExpenseAndMonth(fixed, month)).isTrue();

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(repository).save(captor.capture());
        Expense saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Rent");
        assertThat(saved.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(saved.getExpenseCategory()).isEqualTo(category);
        assertThat(saved.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getFixedExpense()).isSameAs(fixed);
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(saved.getPaidAt()).isNull();
    }

    @Test
    void createByFixedExpenseAndMonth_dueDayAfterMonthEnd_shouldUseLastDayOfMonth() {
        FixedExpense fixed = fixedExpense((short) 31);
        YearMonth february = YearMonth.of(2026, 2);
        when(repository.existsByFixedExpenseIdAndDueDateBetween(any(), any(), any())).thenReturn(false);

        service.createByFixedExpenseAndMonth(fixed, february);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDueDate()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    private FixedExpense fixedExpense(short dueDay) {
        FixedExpense fixed = new FixedExpense();
        ReflectionTestUtils.setField(fixed, "id", UUID.randomUUID());
        fixed.setName("Rent");
        fixed.setAmount(new BigDecimal("1500.00"));
        fixed.setDueDay(dueDay);
        fixed.setExpenseCategory(category);
        fixed.setPaymentMethod(paymentMethod);
        fixed.setUser(user);
        return fixed;
    }

    @Test
    void update_withRelations_shouldChangeCategoryAndPaymentMethod() {
        ExpenseCategory newCategory = new ExpenseCategory();
        PaymentMethod newPaymentMethod = new PaymentMethod();
        UUID newCategoryId = UUID.randomUUID();
        UUID newPaymentMethodId = UUID.randomUUID();
        var request = new UpdateExpenseRequest(null, null, newCategoryId, newPaymentMethodId, null, null);
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.of(entity));
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(newCategoryId, userId)).thenReturn(newCategory);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(newPaymentMethodId, userId)).thenReturn(newPaymentMethod);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(expenseId, request, userId)).isEqualTo(response);

        assertThat(entity.getExpenseCategory()).isSameAs(newCategory);
        assertThat(entity.getPaymentMethod()).isSameAs(newPaymentMethod);
        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void update_withoutRelations_shouldKeepCategoryAndPaymentMethod() {
        var request = new UpdateExpenseRequest("Other", null, null, null, null, LocalDate.now());
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        service.update(expenseId, request, userId);

        assertThat(entity.getExpenseCategory()).isSameAs(category);
        assertThat(entity.getPaymentMethod()).isSameAs(paymentMethod);
        verifyNoInteractions(expenseCategoryService, paymentMethodService);
        verify(repository).save(entity);
    }

    @Test
    void update_whenNotOwned_shouldThrowAndNotSave() {
        var request = new UpdateExpenseRequest("Other", null, null, null, null, null);
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(expenseId, request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenNewPaymentMethodNotOwned_shouldThrowAndNotSave() {
        UUID otherPaymentMethodId = UUID.randomUUID();
        var request = new UpdateExpenseRequest(null, null, null, otherPaymentMethodId, null, null);
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.of(entity));
        when(paymentMethodService.getEntityOwnedByIdOrThrow(otherPaymentMethodId, userId))
                .thenThrow(NotFoundException.of("Payment method", otherPaymentMethodId));

        assertThatThrownBy(() -> service.update(expenseId, request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteOwnedEntity() {
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.of(entity));

        service.delete(expenseId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotOwned_shouldNotDelete() {
        when(repository.findByIdAndUserId(expenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(expenseId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
