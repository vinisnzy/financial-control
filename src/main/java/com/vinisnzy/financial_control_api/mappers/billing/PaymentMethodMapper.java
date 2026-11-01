package com.vinisnzy.financial_control_api.mappers.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.PaymentMethodResponse;
import com.vinisnzy.financial_control_api.entities.billing.PaymentMethod;
import com.vinisnzy.financial_control_api.mappers.annotation.IgnoreAuditFields;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {

    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    PaymentMethodResponse toResponse(PaymentMethod entity);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "fixedExpenses", ignore = true)
    @Mapping(target = "installmentExpenses", ignore = true)
    PaymentMethod toEntity(CreatePaymentMethodRequest request);

    @IgnoreAuditFields
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "fixedExpenses", ignore = true)
    @Mapping(target = "installmentExpenses", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdatePaymentMethodRequest request, @MappingTarget PaymentMethod entity);
}
