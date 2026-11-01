package com.vinisnzy.financial_control_api.mappers.income;

import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeCategoryResponse;
import com.vinisnzy.financial_control_api.entities.income.IncomeCategory;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IncomeCategoryMapper {

    IncomeCategoryResponse toResponse(IncomeCategory entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "incomes", ignore = true)
    @Mapping(target = "fixedIncomes", ignore = true)
    IncomeCategory toEntity(CreateIncomeCategoryRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "incomes", ignore = true)
    @Mapping(target = "fixedIncomes", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateIncomeCategoryRequest request, @MappingTarget IncomeCategory entity);
}
