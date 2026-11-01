package com.vinisnzy.financial_control_api.mappers.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.BankAccountResponse;
import com.vinisnzy.financial_control_api.entities.billing.BankAccount;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    BankAccountResponse toResponse(BankAccount entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "paymentMethods", ignore = true)
    @Mapping(target = "incomes", ignore = true)
    @Mapping(target = "fixedIncomes", ignore = true)
    BankAccount toEntity(CreateBankAccountRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "paymentMethods", ignore = true)
    @Mapping(target = "incomes", ignore = true)
    @Mapping(target = "fixedIncomes", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateBankAccountRequest request, @MappingTarget BankAccount entity);
}
