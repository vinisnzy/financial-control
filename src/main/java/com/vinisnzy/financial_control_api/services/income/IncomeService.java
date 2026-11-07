package com.vinisnzy.financial_control_api.services.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.entities.income.Income;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import com.vinisnzy.financial_control_api.mappers.income.IncomeMapper;
import com.vinisnzy.financial_control_api.repositories.income.IncomeRepository;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import com.vinisnzy.financial_control_api.services.billing.BankAccountService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class IncomeService {
    private final IncomeRepository repository;
    private final IncomeMapper mapper;
    private final UserService userService;
    private final IncomeCategoryService incomeCategoryService;
    private final BankAccountService bankAccountService;

    public IncomeService(IncomeRepository repository, IncomeMapper mapper, UserService userService, IncomeCategoryService incomeCategoryService, BankAccountService bankAccountService, FixedIncomeService fixedIncomeService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
        this.incomeCategoryService = incomeCategoryService;
        this.bankAccountService = bankAccountService;
    }

    private Income getEntityOwnerById(UUID id, UUID userId) {
        return this.repository.findByIdAndUserId(id, userId).orElseThrow(() -> NotFoundException.of("Income", id));
    }

    public PageResponse<IncomeResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserId(userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<IncomeResponse> getAll(LocalDate start, LocalDate end, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByUserIdAndExpectedAtBetween(userId, start, end, pageable), this.mapper::toResponse);
    }

    public IncomeResponse getById(UUID id, UUID userId) {
        return this.mapper.toResponse(getEntityOwnerById(id, userId));
    }

    public PageResponse<IncomeResponse> getByName(String name, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByNameContainingIgnoreCaseAndUserId(name, userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<IncomeResponse> getByBankAccount(UUID bankAccountId, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByBankAccountIdAndUserId(bankAccountId, userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<IncomeResponse> getByBankAccount(UUID bankAccountId, LocalDate start, LocalDate end, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByBankAccountIdAndUserIdAndExpectedAtBetween(bankAccountId, userId, start, end, pageable), this.mapper::toResponse);
    }

    public PageResponse<IncomeResponse> getByIncomeCategory(UUID incomeCategoryId, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByIncomeCategoryIdAndUserId(incomeCategoryId, userId, pageable), this.mapper::toResponse);
    }

    public PageResponse<IncomeResponse> getByIncomeCategory(UUID incomeCategoryId, LocalDate start, LocalDate end, UUID userId, Pageable pageable) {
        return PageResponse.from(this.repository.findByIncomeCategoryIdAndUserIdAndExpectedAtBetween(incomeCategoryId, userId, start, end, pageable), this.mapper::toResponse);
    }

    public IncomeResponse create(UUID userId, CreateIncomeRequest data) {
        Income entity = this.mapper.toEntity(data);

        User user = this.userService.getEntity(userId);
        IncomeCategory incomeCategory = this.incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(data.incomeCategoryId(), userId);
        BankAccount bankAccount = this.bankAccountService.getEntityOwnedByIdOrThrow(data.bankAccountId(), userId);

        entity.setUser(user);
        entity.setIncomeCategory(incomeCategory);
        entity.setBankAccount(bankAccount);

        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public boolean createByFixedIncomeAndMonth(FixedIncome fixedIncome, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        if (this.repository.existsByFixedIncomeIdAndExpectedAtBetween(fixedIncome.getId(), start, end)) return false;

        int day = Math.min(fixedIncome.getPayday(), month.lengthOfMonth());
        Income income = new Income();
        income.setName(fixedIncome.getName());
        income.setAmount(fixedIncome.getAmount());
        income.setIncomeCategory(fixedIncome.getIncomeCategory());
        income.setBankAccount(fixedIncome.getBankAccount());
        income.setExpectedAt(month.atDay(day));
        income.setFixedIncome(fixedIncome);
        income.setUser(fixedIncome.getUser());
        this.repository.save(income);
        return true;
    }

    public IncomeResponse update(UUID id, UpdateIncomeRequest data, UUID userId) {
        Income entity = this.getEntityOwnerById(id, userId);
        this.mapper.updateEntity(data, entity);
        if (data.incomeCategoryId() != null) {
            IncomeCategory incomeCategory = this.incomeCategoryService.getEntityOwnedOrDefaultByIdOrThrow(data.incomeCategoryId(), userId);
            entity.setIncomeCategory(incomeCategory);
        }
        if (data.bankAccountId() != null) {
            BankAccount bankAccount = this.bankAccountService.getEntityOwnedByIdOrThrow(data.bankAccountId(), userId);
            entity.setBankAccount(bankAccount);
        }
        this.repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id, UUID userId) {
        Income entity = this.getEntityOwnerById(id, userId);
        this.repository.delete(entity);
    }
}
