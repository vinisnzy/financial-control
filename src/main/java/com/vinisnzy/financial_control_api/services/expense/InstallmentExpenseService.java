package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.InstallmentExpenseResponse;
import com.vinisnzy.financial_control_api.entities.User;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.entities.expense.InstallmentExpense;
import com.vinisnzy.financial_control_api.mappers.expense.InstallmentExpenseMapper;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseRepository;
import com.vinisnzy.financial_control_api.repositories.expense.InstallmentExpenseRepository;
import com.vinisnzy.financial_control_api.services.UserService;
import com.vinisnzy.financial_control_api.services.billing.PaymentMethodService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InstallmentExpenseService {
    private final InstallmentExpenseRepository repository;
    private final ExpenseRepository expenseRepository;
    private final InstallmentExpenseMapper mapper;
    private final UserService userService;
    private final ExpenseCategoryService expenseCategoryService;
    private final PaymentMethodService paymentMethodService;

    public InstallmentExpenseService(InstallmentExpenseRepository repository,
                                     ExpenseRepository expenseRepository,
                                     InstallmentExpenseMapper mapper,
                                     UserService userService,
                                     ExpenseCategoryService expenseCategoryService,
                                     PaymentMethodService paymentMethodService) {
        this.repository = repository;
        this.expenseRepository = expenseRepository;
        this.mapper = mapper;
        this.userService = userService;
        this.expenseCategoryService = expenseCategoryService;
        this.paymentMethodService = paymentMethodService;
    }

    public InstallmentExpense getEntityOwnerById(UUID id, UUID userId) {
        return this.repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Installment expense not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public PageResponse<InstallmentExpenseResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserId(userId, pageable), this.mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InstallmentExpenseResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(getEntityOwnerById(id, userId));
    }

    @Transactional
    public InstallmentExpenseResponse create(CreateInstallmentExpenseRequest data, UUID userId) {
        InstallmentExpense entity = this.mapper.toEntity(data);

        User user = userService.getEntity(userId);
        ExpenseCategory expenseCategory = expenseCategoryService
                .getEntityOwnedOrDefaultByIdOrThrow(data.expenseCategoryId(), userId);
        PaymentMethod paymentMethod = paymentMethodService.getEntityOwnedByIdOrThrow(data.paymentMethodId(), userId);

        entity.setUser(user);
        entity.setExpenseCategory(expenseCategory);
        entity.setPaymentMethod(paymentMethod);

        this.repository.save(entity);
        this.generateInstallments(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public InstallmentExpenseResponse update(UUID id, UpdateInstallmentExpenseRequest data, UUID userId) {
        InstallmentExpense entity = this.getEntityOwnerById(id, userId);

        boolean scheduleChanged =
                (data.totalAmount() != null && data.totalAmount().compareTo(entity.getTotalAmount()) != 0)
                        || (data.installments() != null && !data.installments().equals(entity.getInstallments()))
                        || (data.firstDueDate() != null && !data.firstDueDate().equals(entity.getFirstDueDate()));

        if (scheduleChanged && entity.getExpenses().stream().anyMatch(e -> e.getPaidAt() != null)) {
            throw new RuntimeException("Cannot change amount, installments or first due date of an installment expense with paid installments");
        }

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

        if (scheduleChanged) {
            this.expenseRepository.deleteAll(entity.getExpenses());
            entity.getExpenses().clear();
            this.generateInstallments(entity);
        }

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        InstallmentExpense entity = this.getEntityOwnerById(id, userId);
        this.repository.delete(entity);
    }

    private void generateInstallments(InstallmentExpense installmentExpense) {
        int count = installmentExpense.getInstallments();
        BigDecimal baseInstallment = installmentExpense.getTotalAmount()
                .divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        BigDecimal remainder = installmentExpense.getTotalAmount()
                .subtract(baseInstallment.multiply(BigDecimal.valueOf(count)));

        List<Expense> expenses = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Expense expense = new Expense();
            expense.setName(installmentExpense.getName() + " (" + i + "/" + count + ")");
            // the last installment absorbs the rounding remainder so the sum matches the total
            expense.setAmount(i == count ? baseInstallment.add(remainder) : baseInstallment);
            expense.setExpenseCategory(installmentExpense.getExpenseCategory());
            expense.setPaymentMethod(installmentExpense.getPaymentMethod());
            expense.setDueDate(installmentExpense.getFirstDueDate().plusMonths(i - 1L));
            expense.setInstallmentExpense(installmentExpense);
            expense.setInstallmentNumber((short) i);
            expense.setUser(installmentExpense.getUser());
            expenses.add(expense);
        }

        this.expenseRepository.saveAll(expenses);
        installmentExpense.getExpenses().addAll(expenses);
    }
}
