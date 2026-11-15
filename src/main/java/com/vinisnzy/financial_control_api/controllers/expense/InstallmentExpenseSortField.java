package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InstallmentExpenseSortField implements SortField {
    NAME("name"),
    TOTAL_AMOUNT("totalAmount"),
    INSTALLMENTS("installments"),
    FIRST_DUE_DATE("firstDueDate"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
