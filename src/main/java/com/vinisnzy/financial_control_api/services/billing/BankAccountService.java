package com.vinisnzy.financial_control_api.services.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.BankAccountResponse;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.entities.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.billing.BankAccountMapper;
import com.vinisnzy.financial_control_api.repositories.billing.BankAccountRepository;
import com.vinisnzy.financial_control_api.services.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BankAccountService {
    private final BankAccountRepository repository;
    private final BankAccountMapper mapper;
    private final UserService userService;

    public BankAccountService(BankAccountRepository repository, BankAccountMapper mapper, UserService userService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
    }

    public BankAccount getEntityOwnedByIdOrThrow(UUID id, UUID userId) {
        return this.repository.findByIdAndUserId(id, userId).orElseThrow(() -> NotFoundException.of("Bank", id));
    }

    public PageResponse<BankAccountResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserId(userId, pageable), this.mapper::toResponse);
    }

    public BankAccountResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(this.getEntityOwnedByIdOrThrow(id, userId));
    }

    public BankAccountResponse create(CreateBankAccountRequest data, UUID userId) {
        BankAccount entity = this.mapper.toEntity(data);
        User user = this.userService.getEntity(userId);
        entity.setUser(user);
        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public BankAccountResponse update(UUID id, UpdateBankAccountRequest data, UUID userId) {
        BankAccount entity = this.getEntityOwnedByIdOrThrow(id, userId);
        this.mapper.updateEntity(data, entity);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        BankAccount entity = this.getEntityOwnedByIdOrThrow(id, userId);
        this.repository.delete(entity);
    }
}
