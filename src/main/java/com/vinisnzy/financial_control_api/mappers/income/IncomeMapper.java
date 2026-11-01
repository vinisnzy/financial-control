package com.vinisnzy.financial_control_api.mappers.income;

import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeResponse;
import com.vinisnzy.financial_control_api.entities.income.Income;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(source = "incomeCategory.id", target = "incomeCategoryId")
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    @Mapping(source = "fixedIncome.id", target = "fixedIncomeId")
    IncomeResponse toResponse(Income entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "incomeCategory", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "fixedIncome", ignore = true)
    Income toEntity(CreateIncomeRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "incomeCategory", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "fixedIncome", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateIncomeRequest request, @MappingTarget Income entity);
}
