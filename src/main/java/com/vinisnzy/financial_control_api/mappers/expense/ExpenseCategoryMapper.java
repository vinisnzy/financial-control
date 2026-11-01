package com.vinisnzy.financial_control_api.mappers.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseCategoryResponse;
import com.vinisnzy.financial_control_api.entities.expense.ExpenseCategory;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper {

    ExpenseCategoryResponse toResponse(ExpenseCategory entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "fixedExpenses", ignore = true)
    @Mapping(target = "installmentExpenses", ignore = true)
    ExpenseCategory toEntity(CreateExpenseCategoryRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "fixedExpenses", ignore = true)
    @Mapping(target = "installmentExpenses", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateExpenseCategoryRequest request, @MappingTarget ExpenseCategory entity);
}
