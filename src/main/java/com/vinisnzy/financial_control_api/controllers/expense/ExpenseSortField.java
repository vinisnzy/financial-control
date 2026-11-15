package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ExpenseSortField implements SortField {
    NAME("name"),
    AMOUNT("amount"),
    DUE_DATE("dueDate"),
    PAID_AT("paidAt"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
