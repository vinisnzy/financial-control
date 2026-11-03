package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.FixedExpenseResponse;
import com.vinisnzy.financial_control_api.entities.User;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.expense.FixedExpenseMapper;
import com.vinisnzy.financial_control_api.repositories.expense.FixedExpenseRepository;
import com.vinisnzy.financial_control_api.services.UserService;
import com.vinisnzy.financial_control_api.services.billing.PaymentMethodService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FixedExpenseService {
    private final FixedExpenseRepository repository;
    private final FixedExpenseMapper mapper;
    private final UserService userService;
    private final ExpenseCategoryService expenseCategoryService;
    private final PaymentMethodService paymentMethodService;

    public FixedExpenseService(FixedExpenseRepository repository,
                               FixedExpenseMapper mapper,
                               UserService userService,
                               ExpenseCategoryService expenseCategoryService,
                               PaymentMethodService paymentMethodService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
        this.expenseCategoryService = expenseCategoryService;
        this.paymentMethodService = paymentMethodService;
    }

    public FixedExpense getEntityOwnerById(UUID id, UUID userId) {
        return this.repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundException.of("Fixed expense", id));
    }

    public PageResponse<FixedExpenseResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserId(userId, pageable), this.mapper::toResponse);
    }

    public FixedExpenseResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(getEntityOwnerById(id, userId));
    }

    public PageResponse<FixedExpenseResponse> getByExpenseCategory(UUID expenseCategoryId, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository
                .findByExpenseCategoryIdAndUserId(expenseCategoryId, userId, pageable), this.mapper::toResponse);
    }

    public FixedExpenseResponse create(CreateFixedExpenseRequest data, UUID userId) {
        FixedExpense entity = this.mapper.toEntity(data);

        User user = userService.getEntity(userId);
        ExpenseCategory expenseCategory = expenseCategoryService
                .getEntityOwnedOrDefaultByIdOrThrow(data.expenseCategoryId(), userId);
        PaymentMethod paymentMethod = paymentMethodService.getEntityOwnedByIdOrThrow(data.paymentMethodId(), userId);

        entity.setUser(user);
        entity.setExpenseCategory(expenseCategory);
        entity.setPaymentMethod(paymentMethod);

        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public FixedExpenseResponse update(UUID id, UpdateFixedExpenseRequest data, UUID userId) {
        FixedExpense entity = this.getEntityOwnerById(id, userId);

        this.mapper.updateEntity(data, entity);

        if (data.expenseCategoryId() != null) {
            ExpenseCategory expenseCategory = expenseCategoryService
                    .getEntityOwnedOrDefaultByIdOrThrow(data.expenseCategoryId(), userId);
            entity.setExpenseCategory(expenseCategory);
        }

        if (data.paymentMethodId() != null) {
            PaymentMethod paymentMethod = paymentMethodService.getEntityOwnedByIdOrThrow(data.paymentMethodId(), userId);
            entity.setPaymentMethod(paymentMethod);
        }

        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        FixedExpense entity = this.getEntityOwnerById(id, userId);
        this.repository.delete(entity);
    }
}
