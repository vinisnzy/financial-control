package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ExpenseCategorySortField implements SortField {
    NAME("name"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
