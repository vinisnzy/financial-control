package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.expense.ExpenseMapper;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseRepository;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import com.vinisnzy.financial_control_api.services.billing.PaymentMethodService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class ExpenseService {
    private final ExpenseRepository repository;
    private final ExpenseMapper mapper;
    private final UserService userService;
    private final ExpenseCategoryService expenseCategoryService;
    private final PaymentMethodService paymentMethodService;

    public ExpenseService(ExpenseRepository repository,
                          ExpenseMapper mapper,
                          UserService userService,
                          ExpenseCategoryService expenseCategoryService,
                          PaymentMethodService paymentMethodService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
        this.expenseCategoryService = expenseCategoryService;
        this.paymentMethodService = paymentMethodService;
    }

    private Expense getEntityOwnerById(UUID id, UUID userId) {
        return this.repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundException.of("Expense", id));
    }

    public PageResponse<ExpenseResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserId(userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<ExpenseResponse> getAll(LocalDate start, LocalDate end, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByUserIdAndDueDateBetween(userId, start, end, pageable), this.mapper::toResponse);
    }

    public ExpenseResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(getEntityOwnerById(id, userId));
    }

    public PageResponse<ExpenseResponse> getByName(String name, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByNameContainingIgnoreCaseAndUserId(name, userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<ExpenseResponse> getByExpenseCategory(UUID expenseCategoryId, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByExpenseCategoryIdAndUserId(expenseCategoryId, userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<ExpenseResponse> getByExpenseCategory(UUID expenseCategoryId, LocalDate start, LocalDate end, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByExpenseCategoryIdAndUserIdAndDueDateBetween(expenseCategoryId, userId, start, end, pageable), this.mapper::toResponse);
    }

    public PageResponse<ExpenseResponse> getByPaymentMethod(UUID paymentMethodId, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByPaymentMethodIdAndUserId(paymentMethodId, userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<ExpenseResponse> getByPaymentMethod(UUID paymentMethodId, LocalDate start, LocalDate end, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByPaymentMethodIdAndUserIdAndDueDateBetween(paymentMethodId, userId, start, end, pageable), this.mapper::toResponse);
    }

    public PageResponse<ExpenseResponse> getByInstallmentExpense(UUID installmentExpenseId, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByInstallmentExpenseIdAndUserId(installmentExpenseId, userId, pageable), this.mapper::toResponse);
    }

    public ExpenseResponse create(CreateExpenseRequest data, UUID userId) {
        Expense entity = this.mapper.toEntity(data);

        User user = this.userService.getEntity(userId);
        ExpenseCategory expenseCategory = this.expenseCategoryService.
                getEntityOwnedOrDefaultByIdOrThrow(data.expenseCategoryId(), userId);
        PaymentMethod paymentMethod = this.paymentMethodService.
                getEntityOwnedByIdOrThrow(data.paymentMethodId(), userId);

        entity.setUser(user);
        entity.setExpenseCategory(expenseCategory);
        entity.setPaymentMethod(paymentMethod);

        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public boolean createByFixedExpenseAndMonth(FixedExpense fixedExpense, YearMonth month) {
        LocalDate first = month.atDay(1);
        LocalDate last = month.atEndOfMonth();
        if (this.repository.
                existsByFixedExpenseIdAndDueDateBetween(fixedExpense.getId(), first, last)) return false;

        int day = Math.min(fixedExpense.getDueDay(), month.lengthOfMonth());
        Expense expense = new Expense();
        expense.setName(fixedExpense.getName());
        expense.setAmount(fixedExpense.getAmount());
        expense.setExpenseCategory(fixedExpense.getExpenseCategory());
        expense.setPaymentMethod(fixedExpense.getPaymentMethod());
        expense.setDueDate(month.atDay(day));
        expense.setFixedExpense(fixedExpense);
        expense.setUser(fixedExpense.getUser());
        this.repository.save(expense);
        return true;
    }

    public ExpenseResponse update(UUID id, UpdateExpenseRequest data, UUID userId) {
        Expense entity = this.getEntityOwnerById(id, userId);
        this.mapper.updateEntity(data, entity);
        if (data.expenseCategoryId() != null) {
            ExpenseCategory expenseCategory = this.expenseCategoryService.
                    getEntityOwnedOrDefaultByIdOrThrow(data.expenseCategoryId(), userId);
            entity.setExpenseCategory(expenseCategory);
        }
        if (data.paymentMethodId() != null) {
            PaymentMethod paymentMethod = this.paymentMethodService.
                    getEntityOwnedByIdOrThrow(data.paymentMethodId(), userId);
            entity.setPaymentMethod(paymentMethod);
        }
        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        Expense entity = this.getEntityOwnerById(id, userId);
        this.repository.delete(entity);
    }
}
