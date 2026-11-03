package com.vinisnzy.financial_control_api.services.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.PaymentMethodResponse;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.entities.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.billing.PaymentMethodMapper;
import com.vinisnzy.financial_control_api.repositories.billing.PaymentMethodRepository;
import com.vinisnzy.financial_control_api.services.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentMethodService {
    private final PaymentMethodRepository repository;
    private final PaymentMethodMapper mapper;
    private final BankAccountService bankAccountService;
    private final UserService userService;

    public PaymentMethodService(
            PaymentMethodRepository repository,
            PaymentMethodMapper mapper,
            BankAccountService bankAccountService,
            UserService userService) {
        this.repository = repository;
        this.mapper = mapper;
        this.bankAccountService = bankAccountService;
        this.userService = userService;
    }

    public PaymentMethod getEntityOwnedByIdOrThrow(UUID id, UUID userId) {
        return this.repository.findByIdAndUserId(id, userId).orElseThrow(() -> NotFoundException.of("Payment method", id));
    }

    public PageResponse<PaymentMethodResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserId(userId, pageable), this.mapper::toResponse);
    }

    public PaymentMethodResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(this.getEntityOwnedByIdOrThrow(id, userId));
    }

    public PageResponse<PaymentMethodResponse> getByBankAccountId(UUID userId, UUID bankAccountId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserIdAndBankAccountId(userId, bankAccountId, pageable), this.mapper::toResponse);
    }

    public PaymentMethodResponse create(CreatePaymentMethodRequest data, UUID userId) {
        PaymentMethod entity = this.mapper.toEntity(data);
        User user = this.userService.getEntity(userId);
        entity.setUser(user);
        BankAccount bankAccount = this.bankAccountService.getEntityOwnedByIdOrThrow(data.bankAccountId(), userId);
        entity.setBankAccount(bankAccount);
        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public PaymentMethodResponse update(UUID id, UpdatePaymentMethodRequest data, UUID userId) {
        PaymentMethod entity = this.getEntityOwnedByIdOrThrow(id, userId);
        if (data.bankAccountId() != null) {
            BankAccount bankAccount = bankAccountService.getEntityOwnedByIdOrThrow(data.bankAccountId(), userId);
            entity.setBankAccount(bankAccount);
        }
        this.mapper.updateEntity(data, entity);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        PaymentMethod entity = this.getEntityOwnedByIdOrThrow(id, userId);
        this.repository.delete(entity);
    }
}
