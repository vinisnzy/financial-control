package com.vinisnzy.financial_control_api.services.income;

import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.entities.income.Income;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.income.IncomeMapper;
import com.vinisnzy.financial_control_api.repositories.income.IncomeRepository;
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
class IncomeServiceTest {

    @Mock
    IncomeRepository repository;
    @Mock
    IncomeMapper mapper;
    @Mock
    UserService userService;
    @Mock
    IncomeCategoryService incomeCategoryService;
    @Mock
    BankAccountService bankAccountService;

    @InjectMocks
    IncomeService service;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 1, 31);

    private UUID userId;
    private UUID incomeId;
    private UUID categoryId;
    private UUID bankAccountId;
    private User user;
    private IncomeCategory category;
    private BankAccount bankAccount;
    private Income entity;
    private IncomeResponse response;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        incomeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        bankAccountId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        category = new IncomeCategory();
        ReflectionTestUtils.setField(category, "id", categoryId);

        bankAccount = new BankAccount();
        ReflectionTestUtils.setField(bankAccount, "id", bankAccountId);

        entity = new Income();
        ReflectionTestUtils.setField(entity, "id", incomeId);
        entity.setName("Salary");
        entity.setAmount(new BigDecimal("5000.00"));
        entity.setUser(user);
        entity.setIncomeCategory(category);
        entity.setBankAccount(bankAccount);
        entity.setExpectedAt(START);

        response = new IncomeResponse(incomeId, "Salary", new BigDecimal("5000.00"), categoryId, bankAccountId,
                START, null, null, Instant.now(), null);
    }

    private PageImpl<Income> page() {
        return new PageImpl<>(List.of(entity), pageable, 1);
    }

    @Test
    void getAll_withoutPeriod_shouldSearchByUser() {
        when(repository.findByUserId(userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getAll(null, null, userId, pageable).content()).containsExactly(response);

        verify(repository, never()).findByUserIdAndExpectedAtBetween(any(), any(), any(), any());
    }

    @Test
    void getAll_withPeriod_shouldSearchByExpectedAtBetween() {
        when(repository.findByUserIdAndExpectedAtBetween(userId, START, END, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getAll(START, END, userId, pageable).content()).containsExactly(response);

        verify(repository, never()).findByUserId(any(), any());
    }

    @Test
    void getAll_withInvalidPeriod_shouldThrowAndNotQuery() {
        assertThatThrownBy(() -> service.getAll(END, START, userId, pageable))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void getById_shouldReturnMappedEntity() {
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getById(incomeId, userId)).isEqualTo(response);
    }

    @Test
    void getById_whenNotOwned_shouldThrow() {
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(incomeId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(incomeId.toString());

        verifyNoInteractions(mapper);
    }

    @Test
    void getByName_shouldMapPage() {
        when(repository.findByNameContainingIgnoreCaseAndUserId("sal", userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByName("sal", userId, pageable).content()).containsExactly(response);
    }

    @Test
    void getByBankAccount_withoutPeriod_shouldSearchByBankAccount() {
        when(repository.findByBankAccountIdAndUserId(bankAccountId, userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByBankAccount(bankAccountId, null, null, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByBankAccount_withPeriod_shouldSearchByBankAccountAndExpectedAt() {
        when(repository.findByBankAccountIdAndUserIdAndExpectedAtBetween(bankAccountId, userId, START, END, pageable))
                .thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByBankAccount(bankAccountId, START, END, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByBankAccount_withInvalidPeriod_shouldThrow() {
        assertThatThrownBy(() -> service.getByBankAccount(bankAccountId, START, null, userId, pageable))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void getByIncomeCategory_withoutPeriod_shouldSearchByCategory() {
        when(repository.findByIncomeCategoryIdAndUserId(categoryId, userId, pageable)).thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByIncomeCategory(categoryId, null, null, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByIncomeCategory_withPeriod_shouldSearchByCategoryAndExpectedAt() {
        when(repository.findByIncomeCategoryIdAndUserIdAndExpectedAtBetween(categoryId, userId, START, END, pageable))
                .thenReturn(page());
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.getByIncomeCategory(categoryId, START, END, userId, pageable).content())
                .containsExactly(response);
    }

    @Test
    void getByIncomeCategory_withInvalidPeriod_shouldThrow() {
        assertThatThrownBy(() -> service.getByIncomeCategory(categoryId, null, END, userId, pageable))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void create_shouldSetRelationsAndSave() {
        var request = new CreateIncomeRequest("Salary", new BigDecimal("5000.00"), categoryId, bankAccountId, START, null);
        Income newEntity = new Income();
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(userService.getEntity(userId)).thenReturn(user);
        when(incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(bankAccountService.getEntityOwnedByIdOrThrow(bankAccountId, userId)).thenReturn(bankAccount);
        when(mapper.toResponse(newEntity)).thenReturn(response);

        assertThat(service.create(userId, request)).isEqualTo(response);

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getIncomeCategory()).isEqualTo(category);
        assertThat(captor.getValue().getBankAccount()).isEqualTo(bankAccount);
    }

    @Test
    void create_whenCategoryNotFound_shouldThrowAndNotSave() {
        var request = new CreateIncomeRequest("Salary", BigDecimal.TEN, categoryId, bankAccountId, START, null);
        when(mapper.toEntity(request)).thenReturn(new Income());
        when(userService.getEntity(userId)).thenReturn(user);
        when(incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId))
                .thenThrow(NotFoundException.of("Income", categoryId));

        assertThatThrownBy(() -> service.create(userId, request)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(bankAccountService);
        verify(repository, never()).save(any());
    }

    @Test
    void create_whenBankAccountNotOwned_shouldThrowAndNotSave() {
        var request = new CreateIncomeRequest("Salary", BigDecimal.TEN, categoryId, bankAccountId, START, null);
        when(mapper.toEntity(request)).thenReturn(new Income());
        when(userService.getEntity(userId)).thenReturn(user);
        when(incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(categoryId, userId)).thenReturn(category);
        when(bankAccountService.getEntityOwnedByIdOrThrow(bankAccountId, userId))
                .thenThrow(NotFoundException.of("Bank", bankAccountId));

        assertThatThrownBy(() -> service.create(userId, request)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    private FixedIncome fixedIncome(short payday) {
        FixedIncome fixed = new FixedIncome();
        ReflectionTestUtils.setField(fixed, "id", UUID.randomUUID());
        fixed.setName("Salary");
        fixed.setAmount(new BigDecimal("5000.00"));
        fixed.setPayday(payday);
        fixed.setIncomeCategory(category);
        fixed.setBankAccount(bankAccount);
        fixed.setUser(user);
        return fixed;
    }

    @Test
    void createByFixedIncomeAndMonth_whenAlreadyGenerated_shouldReturnFalse() {
        FixedIncome fixed = fixedIncome((short) 5);
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.existsByFixedIncomeIdAndExpectedAtBetween(fixed.getId(), month.atDay(1), month.atEndOfMonth()))
                .thenReturn(true);

        assertThat(service.createByFixedIncomeAndMonth(fixed, month)).isFalse();

        verify(repository, never()).save(any());
    }

    @Test
    void createByFixedIncomeAndMonth_shouldCopyFixedIncomeData() {
        FixedIncome fixed = fixedIncome((short) 5);
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.existsByFixedIncomeIdAndExpectedAtBetween(fixed.getId(), month.atDay(1), month.atEndOfMonth()))
                .thenReturn(false);

        assertThat(service.createByFixedIncomeAndMonth(fixed, month)).isTrue();

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(repository).save(captor.capture());
        Income saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Salary");
        assertThat(saved.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(saved.getIncomeCategory()).isEqualTo(category);
        assertThat(saved.getBankAccount()).isEqualTo(bankAccount);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getFixedIncome()).isSameAs(fixed);
        assertThat(saved.getExpectedAt()).isEqualTo(LocalDate.of(2026, 3, 5));
        assertThat(saved.getReceivedAt()).isNull();
    }

    @Test
    void createByFixedIncomeAndMonth_paydayAfterMonthEnd_shouldUseLastDayOfMonth() {
        FixedIncome fixed = fixedIncome((short) 31);
        YearMonth april = YearMonth.of(2026, 4);
        when(repository.existsByFixedIncomeIdAndExpectedAtBetween(any(), any(), any())).thenReturn(false);

        service.createByFixedIncomeAndMonth(fixed, april);

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getExpectedAt()).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    @Test
    void update_withRelations_shouldChangeCategoryAndBankAccount() {
        IncomeCategory newCategory = new IncomeCategory();
        BankAccount newBankAccount = new BankAccount();
        UUID newCategoryId = UUID.randomUUID();
        UUID newBankAccountId = UUID.randomUUID();
        var request = new UpdateIncomeRequest(null, null, newCategoryId, newBankAccountId, null, null);
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.of(entity));
        when(incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(newCategoryId, userId)).thenReturn(newCategory);
        when(bankAccountService.getEntityOwnedByIdOrThrow(newBankAccountId, userId)).thenReturn(newBankAccount);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.update(incomeId, request, userId)).isEqualTo(response);

        assertThat(entity.getIncomeCategory()).isSameAs(newCategory);
        assertThat(entity.getBankAccount()).isSameAs(newBankAccount);
        verify(mapper).updateEntity(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void update_withoutRelations_shouldKeepCategoryAndBankAccount() {
        var request = new UpdateIncomeRequest("Bonus", null, null, null, null, LocalDate.now());
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        service.update(incomeId, request, userId);

        assertThat(entity.getIncomeCategory()).isSameAs(category);
        assertThat(entity.getBankAccount()).isSameAs(bankAccount);
        verifyNoInteractions(incomeCategoryService, bankAccountService);
        verify(repository).save(entity);
    }

    @Test
    void update_whenNotOwned_shouldThrowAndNotSave() {
        var request = new UpdateIncomeRequest("Bonus", null, null, null, null, null);
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(incomeId, request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenNewBankAccountNotOwned_shouldThrowAndNotSave() {
        UUID otherBankAccountId = UUID.randomUUID();
        var request = new UpdateIncomeRequest(null, null, null, otherBankAccountId, null, null);
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.of(entity));
        when(bankAccountService.getEntityOwnedByIdOrThrow(otherBankAccountId, userId))
                .thenThrow(NotFoundException.of("Bank", otherBankAccountId));

        assertThatThrownBy(() -> service.update(incomeId, request, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteOwnedEntity() {
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.of(entity));

        service.delete(incomeId, userId);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenNotOwned_shouldNotDelete() {
        when(repository.findByIdAndUserId(incomeId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(incomeId, userId)).isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
