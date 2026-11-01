package com.vinisnzy.financial_control_api.mappers.income;

import com.vinisnzy.financial_control_api.dtos.income.requests.CreateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.FixedIncomeResponse;
import com.vinisnzy.financial_control_api.entities.income.FixedIncome;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FixedIncomeMapper {

    @Mapping(source = "incomeCategory.id", target = "incomeCategoryId")
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    FixedIncomeResponse toResponse(FixedIncome entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "incomeCategory", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "incomes", ignore = true)
    FixedIncome toEntity(CreateFixedIncomeRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "incomeCategory", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "incomes", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateFixedIncomeRequest request, @MappingTarget FixedIncome entity);
}
