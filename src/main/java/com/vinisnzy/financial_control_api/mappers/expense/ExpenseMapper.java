package com.vinisnzy.financial_control_api.mappers.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseResponse;
import com.vinisnzy.financial_control_api.entities.expense.Expense;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "expenseCategory.id", target = "expenseCategoryId")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    @Mapping(source = "fixedExpense.id", target = "fixedExpenseId")
    @Mapping(source = "installmentExpense.id", target = "installmentExpenseId")
    ExpenseResponse toResponse(Expense entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "fixedExpense", ignore = true)
    @Mapping(target = "installmentExpense", ignore = true)
    @Mapping(target = "installmentNumber", ignore = true)
    Expense toEntity(CreateExpenseRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "fixedExpense", ignore = true)
    @Mapping(target = "installmentExpense", ignore = true)
    @Mapping(target = "installmentNumber", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateExpenseRequest request, @MappingTarget Expense entity);
}
