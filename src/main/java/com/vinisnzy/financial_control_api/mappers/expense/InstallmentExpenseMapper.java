package com.vinisnzy.financial_control_api.mappers.expense;

import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.InstallmentExpenseResponse;
import com.vinisnzy.financial_control_api.entities.expense.InstallmentExpense;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = ExpenseMapper.class)
public interface InstallmentExpenseMapper {

    @Mapping(source = "expenseCategory.id", target = "expenseCategoryId")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    InstallmentExpenseResponse toResponse(InstallmentExpense entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    InstallmentExpense toEntity(CreateInstallmentExpenseRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateInstallmentExpenseRequest request, @MappingTarget InstallmentExpense entity);
}
