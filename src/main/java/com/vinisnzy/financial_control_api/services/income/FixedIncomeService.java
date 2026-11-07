package com.vinisnzy.financial_control_api.services.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FixedIncomeService {
    private final FixedIncomeRepository repository;
    private final FixedIncomeMapper mapper;
    private final UserService userService;
    private final IncomeCategoryService incomeCategoryService;
    private final BankAccountService bankAccountService;
    private final IncomeService incomeService;

    public FixedIncomeService(FixedIncomeRepository repository,
                              FixedIncomeMapper mapper,
                              UserService userService,
                              IncomeCategoryService incomeCategoryService,
                              BankAccountService bankAccountService,
                              IncomeService incomeService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
        this.incomeCategoryService = incomeCategoryService;
        this.bankAccountService = bankAccountService;
        this.incomeService = incomeService;
    }

    public FixedIncome getEntityOwnerById(UUID id, UUID userId) {
        return this.repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundException.of("Fixed income", id));
    }

    public PageResponse<FixedIncomeResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserId(userId, pageable), this.mapper::toResponse);
    }

    public FixedIncomeResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(getEntityOwnerById(id, userId));
    }

    public FixedIncomeResponse create(CreateFixedIncomeRequest data, UUID userId) {
        FixedIncome entity = this.mapper.toEntity(data);

        User user = userService.getEntity(userId);
        IncomeCategory incomeCategory = incomeCategoryService
                .getEntityOwnedOrDefaultByIdOrThrow(data.incomeCategoryId(), userId);
        BankAccount bankAccount = bankAccountService.getEntityOwnedByIdOrThrow(data.bankAccountId(), userId);

        entity.setUser(user);
        entity.setIncomeCategory(incomeCategory);
        entity.setBankAccount(bankAccount);

        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public int generateForMonth(YearMonth month) {
        List<FixedIncome> activeFixedIncomes = this.repository
                .findActiveBetween(month.atDay(1), month.atEndOfMonth());
        int count = 0;
        for (FixedIncome fixedIncome : activeFixedIncomes) {
            try {
                if (this.incomeService.createByFixedIncomeAndMonth(fixedIncome, month)) count++;
            } catch (Exception e) {
                log.error("Failed to generate income for fixed income {}", fixedIncome.getId(), e);
            }
        }
        return count;
    }

    public FixedIncomeResponse update(UUID id, UpdateFixedIncomeRequest data, UUID userId) {
        FixedIncome entity = this.getEntityOwnerById(id, userId);

        this.mapper.updateEntity(data, entity);

        if (data.incomeCategoryId() != null) {
            IncomeCategory incomeCategory = incomeCategoryService
                    .getEntityOwnedOrDefaultByIdOrThrow(data.incomeCategoryId(), userId);
            entity.setIncomeCategory(incomeCategory);
        }

        if (data.bankAccountId() != null) {
            BankAccount bankAccount = bankAccountService.getEntityOwnedByIdOrThrow(data.bankAccountId(), userId);
            entity.setBankAccount(bankAccount);
        }

        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        FixedIncome entity = this.getEntityOwnerById(id, userId);
        this.repository.delete(entity);
    }
}
