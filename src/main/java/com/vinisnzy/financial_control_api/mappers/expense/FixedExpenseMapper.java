package com.vinisnzy.financial_control_api.mappers.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.FixedExpenseResponse;
import com.vinisnzy.financial_control_api.entities.expense.FixedExpense;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FixedExpenseMapper {

    @Mapping(source = "expenseCategory.id", target = "expenseCategoryId")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    FixedExpenseResponse toResponse(FixedExpense entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    FixedExpense toEntity(CreateFixedExpenseRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateFixedExpenseRequest request, @MappingTarget FixedExpense entity);
}
