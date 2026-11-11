package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.InstallmentExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.InstallmentExpense;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.expense.InstallmentExpenseMapper;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseRepository;
import com.vinisnzy.financial_control_api.repositories.expense.InstallmentExpenseRepository;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import com.vinisnzy.financial_control_api.services.billing.PaymentMethodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallmentExpenseServiceTest {

    @Mock
    InstallmentExpenseRepository repository;
    @Mock
    ExpenseRepository expenseRepository;
    @Mock
    InstallmentExpenseMapper mapper;
    @Mock
    UserService userService;
    @Mock
    ExpenseCategoryService expenseCategoryService;
    @Mock
    PaymentMethodService paymentMethodService;

    @InjectMocks
    InstallmentExpenseService service;

    @Captor
    ArgumentCaptor<List<Expense>> expensesCaptor;

    private static final LocalDate FIRST_DUE_DATE = LocalDate.of(2026, 1, 31);

    private UUID userId;
    private UUID installmentId;
    private UUID categoryId;
    private UUID paymentMethodId;
    private User user;
    private ExpenseCategory category;
    private PaymentMethod paymentMethod;
    private InstallmentExpense entity;
    private InstallmentExpenseResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        installmentId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        category = new ExpenseCategory();
        ReflectionTestUtils.setField(category, "id", categoryId);

        paymentMethod = new PaymentMethod();
        ReflectionTestUtils.setField(paymentMethod, "id", paymentMethodId);

        entity = installment(new BigDecimal("100.00"), (short) 3);
        ReflectionTestUtils.setField(entity, "id", installmentId);

        response = new InstallmentExpenseResponse(installmentId, "Notebook", new BigDecimal("100.00"), (short) 3,
                categoryId, paymentMethodId, FIRST_DUE_DATE, List.of(), Instant.now(), null);
    }

    private InstallmentExpense installment(BigDecimal totalAmount, short installments) {
        InstallmentExpense installment = new InstallmentExpense();
        installment.setName("Notebook");
        installment.setTotalAmount(totalAmount);
        installment.setInstallments(installments);
        installment.setFirstDueDate(FIRST_DUE_DATE);
        installment.setUser(user);
        installment.setExpenseCategory(category);
        installment.setPaymentMethod(paymentMethod);
        return installment;
    }

    private Expense part(LocalDate paidAt) {
        Expense expense = new Expense();
        expense.setInstallmentExpense(entity);
        expense.setPaidAt(paidAt);
        return expense;
    }

    private void stubCreate(CreateInstallmentExpenseRequest request, InstallmentExpense newEntity) {
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(paymentMethodId, userId)).thenReturn(paymentMethod);
        when(mapper.toResponse(newEntity)).thenReturn(response);
    }

    private CreateInstallmentExpenseRequest createRequest(BigDecimal totalAmount, short installments) {
        return new CreateInstallmentExpenseRequest("Notebook", totalAmount, installments, categoryId,
                paymentMethodId, FIRST_DUE_DATE);
    }

    @Test
    void getEntityOwnerById_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityOwnerById(installmentId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(installmentId.toString());
    }

    @Test
    void getAll_shouldMapPage() {
        var pageable = PageRequest.of(0, 10);
        when(repository.findByUserId(userId, pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getAll(userId, pageable).content()).containsExactly(response);
    }

    @Test
    void getById_shouldReturnMappedEntity() {
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(installmentId, userId)).isEqualTo(response);
    }

    @Test
    void create_shouldSetRelationsAndSave() {
        var request = createRequest(new BigDecimal("100.00"), (short) 3);
        InstallmentExpense newEntity = installment(new BigDecimal("100.00"), (short) 3);
        newEntity.setUser(null);
        newEntity.setExpenseCategory(null);
        newEntity.setPaymentMethod(null);
        stubCreate(request, newEntity);

        assertThat(service.create(request, userId)).isEqualTo(response);

        verify(repository).save(newEntity);
        assertThat(newEntity.getUser()).isEqualTo(user);
        assertThat(newEntity.getExpenseCategory()).isEqualTo(category);
        assertThat(newEntity.getPaymentMethod()).isEqualTo(paymentMethod);
    }

    @Test
    void create_shouldGenerateOneExpensePerInstallment() {
        var request = createRequest(new BigDecimal("100.00"), (short) 3);
        InstallmentExpense newEntity = installment(new BigDecimal("100.00"), (short) 3);
        stubCreate(request, newEntity);

        service.create(request, userId);

        verify(expenseRepository).saveAll(expensesCaptor.capture());
        List<Expense> expenses = expensesCaptor.getValue();

        assertThat(expenses).hasSize(3);
        assertThat(expenses).extracting(Expense::getName)
                .containsExactly("Notebook (1/3)", "Notebook (2/3)", "Notebook (3/3)");
        assertThat(expenses).extracting(Expense::getInstallmentNumber)
                .containsExactly((short) 1, (short) 2, (short) 3);
        assertThat(expenses).allSatisfy(expense -> {
            assertThat(expense.getInstallmentExpense()).isSameAs(newEntity);
            assertThat(expense.getUser()).isEqualTo(user);
            assertThat(expense.getExpenseCategory()).isEqualTo(category);
            assertThat(expense.getPaymentMethod()).isEqualTo(paymentMethod);
            assertThat(expense.getPaidAt()).isNull();
        });
        assertThat(newEntity.getExpenses()).containsExactlyElementsOf(expenses);
    }

    @Test
    void create_lastInstallmentShouldAbsorbRoundingRemainder() {
        var request = createRequest(new BigDecimal("100.00"), (short) 3);
        InstallmentExpense newEntity = installment(new BigDecimal("100.00"), (short) 3);
        stubCreate(request, newEntity);

        service.create(request, userId);

        verify(expenseRepository).saveAll(expensesCaptor.capture());
        List<Expense> expenses = expensesCaptor.getValue();

        assertThat(expenses).extracting(Expense::getAmount)
                .containsExactly(new BigDecimal("33.33"), new BigDecimal("33.33"), new BigDecimal("33.34"));
        assertThat(expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void create_exactDivision_shouldGenerateEqualInstallments() {
        var request = createRequest(new BigDecimal("120.00"), (short) 4);
        InstallmentExpense newEntity = installment(new BigDecimal("120.00"), (short) 4);
        stubCreate(request, newEntity);

        service.create(request, userId);

        verify(expenseRepository).saveAll(expensesCaptor.capture());
        assertThat(expensesCaptor.getValue()).extracting(Expense::getAmount)
                .allSatisfy(amount -> assertThat(amount).isEqualByComparingTo("30.00"));
    }

    @Test
    void create_singleInstallment_shouldUseTotalAmount() {
        var request = createRequest(new BigDecimal("99.99"), (short) 1);
        InstallmentExpense newEntity = installment(new BigDecimal("99.99"), (short) 1);
        stubCreate(request, newEntity);

        service.create(request, userId);

        verify(expenseRepository).saveAll(expensesCaptor.capture());
        assertThat(expensesCaptor.getValue()).singleElement().satisfies(expense -> {
            assertThat(expense.getAmount()).isEqualByComparingTo("99.99");
            assertThat(expense.getName()).isEqualTo("Notebook (1/1)");
            assertThat(expense.getDueDate()).isEqualTo(FIRST_DUE_DATE);
        });
    }

    @Test
    void create_shouldScheduleDueDatesMonthlyClampingToMonthEnd() {
        var request = createRequest(new BigDecimal("300.00"), (short) 3);
        InstallmentExpense newEntity = installment(new BigDecimal("300.00"), (short) 3);
        stubCreate(request, newEntity);

        service.create(request, userId);

        verify(expenseRepository).saveAll(expensesCaptor.capture());
        assertThat(expensesCaptor.getValue()).extracting(Expense::getDueDate).containsExactly(
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 31));
    }

    @Test
    void create_whenCategoryNotFound_shouldThrowAndNotGenerate() {
        var request = createRequest(new BigDecimal("100.00"), (short) 3);
        when(mapper.toEntity(request)).thenReturn(new InstallmentExpense());
        when(userService.getEntity(userId)).thenReturn(user);
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId))
                .thenThrow(NotFoundException.of("Expense category", categoryId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void update_withoutScheduleChange_shouldNotRegenerateInstallments() {
        entity.getExpenses().addAll(List.of(part(LocalDate.now()), part(null), part(null)));
        var request = new UpdateInstallmentExpenseRequest("Laptop", null, null, null, null, null);
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(installmentId, request, userId)).isEqualTo(response);

        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
        verifyNoInteractions(expenseRepository);
        assertThat(entity.getExpenses()).hasSize(3);
    }

    @Test
    void update_sameScheduleValues_shouldNotBeConsideredChange() {
        entity.getExpenses().add(part(LocalDate.now()));
        var request = new UpdateInstallmentExpenseRequest(null, new BigDecimal("100.0"), (short) 3, null, null,
                FIRST_DUE_DATE);
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        service.update(installmentId, request, userId);

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void update_scheduleChangeWithPaidInstallments_shouldThrow() {
        entity.getExpenses().addAll(List.of(part(LocalDate.now()), part(null)));
        var request = new UpdateInstallmentExpenseRequest(null, new BigDecimal("200.00"), null, null, null, null);
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(installmentId, request, userId))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.INSTALLMENT_HAS_PAID_PARTS);

        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).save(any());
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void update_installmentsChangeWithPaidInstallments_shouldThrow() {
        entity.getExpenses().add(part(LocalDate.now()));
        var request = new UpdateInstallmentExpenseRequest(null, null, (short) 6, null, null, null);
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(installmentId, request, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_firstDueDateChangeWithPaidInstallments_shouldThrow() {
        entity.getExpenses().add(part(LocalDate.now()));
        var request = new UpdateInstallmentExpenseRequest(null, null, null, null, null, FIRST_DUE_DATE.plusDays(1));
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(installmentId, request, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_scheduleChangeWithoutPaidInstallments_shouldRegenerateInstallments() {
        List<Expense> oldExpenses = new ArrayList<>(List.of(part(null), part(null), part(null)));
        entity.getExpenses().addAll(oldExpenses);
        var request = new UpdateInstallmentExpenseRequest(null, null, (short) 4, null, null, null);
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));
        doAnswer(invocation -> {
            entity.setInstallments((short) 4);
            return null;
        }).when(mapper).updateEntity(request, entity);
        when(mapper.toResponse(entity)).thenReturn(response);
        List<Expense> deleted = new ArrayList<>();
        doAnswer(invocation -> {
            Iterable<Expense> expenses = invocation.getArgument(0);
            expenses.forEach(deleted::add);
            return null;
        }).when(expenseRepository).deleteAll(anyIterable());

        service.update(installmentId, request, userId);

        verify(repository).save(entity);
        assertThat(deleted).containsExactlyElementsOf(oldExpenses);
        verify(expenseRepository).saveAll(expensesCaptor.capture());
        assertThat(expensesCaptor.getValue()).hasSize(4)
                .extracting(Expense::getAmount)
                .allSatisfy(amount -> assertThat(amount).isEqualByComparingTo("25.00"));
        assertThat(entity.getExpenses()).hasSize(4).doesNotContainAnyElementsOf(oldExpenses);
    }

    @Test
    void update_withRelations_shouldChangeCategoryAndPaymentMethod() {
        ExpenseCategory newCategory = new ExpenseCategory();
        PaymentMethod newPaymentMethod = new PaymentMethod();
        UUID newCategoryId = UUID.randomUUID();
        UUID newPaymentMethodId = UUID.randomUUID();
        var request = new UpdateInstallmentExpenseRequest(null, null, null, newCategoryId, newPaymentMethodId, null);
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));
        when(expenseCategoryService.getEntityOwnedOrDefaultByIdOrThrow(newCategoryId, userId)).thenReturn(newCategory);
        when(paymentMethodService.getEntityOwnedByIdOrThrow(newPaymentMethodId, userId)).thenReturn(newPaymentMethod);
        when(mapper.toResponse(entity)).thenReturn(response);

        service.update(installmentId, request, userId);

        assertThat(entity.getExpenseCategory()).isSameAs(newCategory);
        assertThat(entity.getPaymentMethod()).isSameAs(newPaymentMethod);
        verify(expenseRepository, never()).saveAll(anyList());
    }

    @Test
    void update_whenNotOwned_shouldThrowAndNotSave() {
        var request = new UpdateInstallmentExpenseRequest("Laptop", null, null, null, null, null);
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(installmentId, request, userId))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteOwnedEntity() {
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.of(entity));

        service.delete(installmentId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotOwned_shouldNotDelete() {
        when(repository.findByIdAndUserId(installmentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(installmentId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
