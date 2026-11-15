package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FixedExpenseSortField implements SortField {
    NAME("name"),
    AMOUNT("amount"),
    DUE_DAY("dueDay"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
