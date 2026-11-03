package com.vinisnzy.financial_control_api.services.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseCategoryResponse;
import com.vinisnzy.financial_control_api.entities.User;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.expense.ExpenseCategoryMapper;
import com.vinisnzy.financial_control_api.repositories.expense.ExpenseCategoryRepository;
import com.vinisnzy.financial_control_api.services.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExpenseCategoryService {
    private final ExpenseCategoryRepository repository;
    private final ExpenseCategoryMapper mapper;
    private final UserService userService;

    public ExpenseCategoryService(ExpenseCategoryRepository repository, ExpenseCategoryMapper mapper, UserService userService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
    }

    public ExpenseCategory getEntityOwnedOrDefaultByIdOrThrow(UUID id, UUID userId) {
        return this.repository.findByIdAndUserIdOrDefault(id, userId)
                .orElseThrow(() -> NotFoundException.of("Expense category", id));
    }

    public PageResponse<ExpenseCategoryResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserIdOrDefault(userId, pageable), this.mapper::toResponse);
    }

    public ExpenseCategoryResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(this.getEntityOwnedOrDefaultByIdOrThrow(id, userId));
    }

    public ExpenseCategoryResponse create(CreateExpenseCategoryRequest data, UUID userId) {
        ExpenseCategory entity = this.mapper.toEntity(data);
        User user = this.userService.getEntity(userId);
        entity.setUser(user);
        this.repository.save(entity);
        return this.mapper.toResponse(entity);
    }

    public ExpenseCategoryResponse update(UUID id, UpdateExpenseCategoryRequest data, UUID userId) {
        ExpenseCategory entity = this.getEntityOwnedOrDefaultByIdOrThrow(id, userId);
        this.mapper.updateEntity(data, entity);
        this.repository.save(entity);
        return this.mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        ExpenseCategory entity = this.getEntityOwnedOrDefaultByIdOrThrow(id, userId);
        this.repository.delete(entity);
    }
}
