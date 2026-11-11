package com.vinisnzy.financial_control_api.services.income;

import com.vinisnzy.financial_control_api.dtos.income.requests.CreateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.FixedIncomeResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.income.FixedIncomeMapper;
import com.vinisnzy.financial_control_api.repositories.income.FixedIncomeRepository;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import com.vinisnzy.financial_control_api.services.billing.BankAccountService;
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
class FixedIncomeServiceTest {

    @Mock
    FixedIncomeRepository repository;
    @Mock
    FixedIncomeMapper mapper;
    @Mock
    UserService userService;
    @Mock
    IncomeCategoryService incomeCategoryService;
    @Mock
    BankAccountService bankAccountService;
    @Mock
    IncomeService incomeService;

    @InjectMocks
    FixedIncomeService service;

    private UUID userId;
    private UUID fixedIncomeId;
    private UUID categoryId;
    private UUID bankAccountId;
    private User user;
    private IncomeCategory category;
    private BankAccount bankAccount;
    private FixedIncome entity;
    private FixedIncomeResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        fixedIncomeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        bankAccountId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        category = new IncomeCategory();
        ReflectionTestUtils.setField(category, "id", categoryId);

        bankAccount = new BankAccount();
        ReflectionTestUtils.setField(bankAccount, "id", bankAccountId);

        entity = new FixedIncome();
        ReflectionTestUtils.setField(entity, "id", fixedIncomeId);
        entity.setName("Salary");
        entity.setAmount(new BigDecimal("5000.00"));
        entity.setPayday((short) 5);
        entity.setStartDate(LocalDate.of(2026, 1, 1));
        entity.setUser(user);
        entity.setIncomeCategory(category);
        entity.setBankAccount(bankAccount);

        response = new FixedIncomeResponse(fixedIncomeId, "Salary", new BigDecimal("5000.00"), categoryId,
                bankAccountId, (short) 5, LocalDate.of(2026, 1, 1), null, Instant.now(), null);
    }

    @Test
    void getEntityOwnerById_shouldReturnEntity() {
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.of(entity));

        assertThat(service.getEntityOwnerById(fixedIncomeId, userId)).isSameAs(entity);
    }

    @Test
    void getEntityOwnerById_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityOwnerById(fixedIncomeId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(fixedIncomeId.toString());
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
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(fixedIncomeId, userId)).isEqualTo(response);
    }

    @Test
    void create_shouldSetRelationsAndSave() {
        var request = new CreateFixedIncomeRequest("Salary", new BigDecimal("5000.00"), categoryId, bankAccountId,
                (short) 5, LocalDate.of(2026, 1, 1), null);
        FixedIncome newEntity = new FixedIncome();
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(bankAccountService.getEntityOwnedByIdOrThrow(bankAccountId, userId)).thenReturn(bankAccount);
        when(mapper.toResponse(newEntity)).thenReturn(response);

        assertThat(service.create(request, userId)).isEqualTo(response);

        ArgumentCaptor<FixedIncome> captor = ArgumentCaptor.forClass(FixedIncome.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getIncomeCategory()).isEqualTo(category);
        assertThat(captor.getValue().getBankAccount()).isEqualTo(bankAccount);
    }

    @Test
    void create_whenBankAccountNotOwned_shouldThrowAndNotSave() {
        var request = new CreateFixedIncomeRequest("Salary", BigDecimal.TEN, categoryId, bankAccountId,
                (short) 5, LocalDate.of(2026, 1, 1), null);
        when(mapper.toEntity(request)).thenReturn(new FixedIncome());
        when(userService.getEntity(userId)).thenReturn(user);
        when(incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(bankAccountService.getEntityOwnedByIdOrThrow(bankAccountId, userId))
                .thenThrow(NotFoundException.of("Bank", bankAccountId));

        assertThatThrownBy(() -> service.create(request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void generateForMonth_shouldCountOnlyCreatedIncomes() {
        YearMonth month = YearMonth.of(2026, 3);
        FixedIncome first = new FixedIncome();
        FixedIncome second = new FixedIncome();
        when(repository.findActiveBetween(month.atDay(1), month.atEndOfMonth())).thenReturn(List.of(first, second));
        when(incomeService.createByFixedIncomeAndMonth(first, month)).thenReturn(true);
        when(incomeService.createByFixedIncomeAndMonth(second, month)).thenReturn(false);

        assertThat(service.generateForMonth(month)).isEqualTo(1);
    }

    @Test
    void generateForMonth_whenOneFails_shouldContinueWithOthers() {
        YearMonth month = YearMonth.of(2026, 3);
        FixedIncome failing = new FixedIncome();
        FixedIncome ok = new FixedIncome();
        when(repository.findActiveBetween(month.atDay(1), month.atEndOfMonth())).thenReturn(List.of(failing, ok));
        when(incomeService.createByFixedIncomeAndMonth(failing, month)).thenThrow(new RuntimeException("boom"));
        when(incomeService.createByFixedIncomeAndMonth(ok, month)).thenReturn(true);

        assertThat(service.generateForMonth(month)).isEqualTo(1);

        verify(incomeService).createByFixedIncomeAndMonth(ok, month);
    }

    @Test
    void generateForMonth_withoutActiveFixedIncomes_shouldReturnZero() {
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.findActiveBetween(month.atDay(1), month.atEndOfMonth())).thenReturn(List.of());

        assertThat(service.generateForMonth(month)).isZero();

        verifyNoInteractions(incomeService);
    }

    @Test
    void update_withRelations_shouldChangeCategoryAndBankAccount() {
        IncomeCategory newCategory = new IncomeCategory();
        BankAccount newBankAccount = new BankAccount();
        UUID newCategoryId = UUID.randomUUID();
        UUID newBankAccountId = UUID.randomUUID();
        var request = new UpdateFixedIncomeRequest(null, null, newCategoryId, newBankAccountId, null, null, null);
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.of(entity));
        when(incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(newCategoryId, userId)).thenReturn(newCategory);
        when(bankAccountService.getEntityOwnedByIdOrThrow(newBankAccountId, userId)).thenReturn(newBankAccount);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(fixedIncomeId, request, userId)).isEqualTo(response);

        assertThat(entity.getIncomeCategory()).isSameAs(newCategory);
        assertThat(entity.getBankAccount()).isSameAs(newBankAccount);
        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void update_withoutRelations_shouldKeepCategoryAndBankAccount() {
        var request = new UpdateFixedIncomeRequest("Other", null, null, null, (short) 10, null, null);
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        service.update(fixedIncomeId, request, userId);

        assertThat(entity.getIncomeCategory()).isSameAs(category);
        assertThat(entity.getBankAccount()).isSameAs(bankAccount);
        verifyNoInteractions(incomeCategoryService, bankAccountService);
    }

    @Test
    void update_whenNotOwned_shouldThrowAndNotSave() {
        var request = new UpdateFixedIncomeRequest("Other", null, null, null, null, null, null);
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(fixedIncomeId, request, userId))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteOwnedEntity() {
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.of(entity));

        service.delete(fixedIncomeId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotOwned_shouldNotDelete() {
        when(repository.findByIdAndUserId(fixedIncomeId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(fixedIncomeId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
