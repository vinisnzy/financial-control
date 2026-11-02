package com.vinisnzy.financial_control_api.services.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeCategoryResponse;
import com.vinisnzy.financial_control_api.entities.User;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import com.vinisnzy.financial_control_api.mappers.income.IncomeCategoryMapper;
import com.vinisnzy.financial_control_api.repositories.income.IncomeCategoryRepository;
import com.vinisnzy.financial_control_api.services.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IncomeCategoryService {
    private final IncomeCategoryRepository repository;
    private final IncomeCategoryMapper mapper;
    private final UserService userService;

    public IncomeCategoryService(IncomeCategoryRepository repository, IncomeCategoryMapper mapper, UserService userService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
    }

    public IncomeCategory getEntityOwnedOrDefaultByIdOrThrow(UUID id, UUID userId) {
        return this.repository.findByIdAndUserIdOrDefault(id, userId)
                .orElseThrow(() -> new RuntimeException("Income category not found with id: " + id));
    }

    public PageResponse<IncomeCategoryResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserIdOrDefault(userId, pageable), this.mapper::toResponse);
    }

    public IncomeCategoryResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(this.getEntityOwnedOrDefaultByIdOrThrow(id, userId));
    }

    public IncomeCategoryResponse create(CreateIncomeCategoryRequest data, UUID userId) {
        IncomeCategory entity = this.mapper.toEntity(data);
        User user = this.userService.getEntity(userId);
        entity.setUser(user);
        this.repository.save(entity);
        return this.mapper.toResponse(entity);
    }

    public IncomeCategoryResponse update(UUID id, UpdateIncomeCategoryRequest data, UUID userId) {
        IncomeCategory entity = this.getEntityOwnedOrDefaultByIdOrThrow(id, userId);
        this.mapper.updateEntity(data, entity);
        this.repository.save(entity);
        return this.mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        IncomeCategory entity = this.getEntityOwnedOrDefaultByIdOrThrow(id, userId);
        this.repository.delete(entity);
    }
}
