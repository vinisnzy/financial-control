package com.vinisnzy.financial_control_api.controllers.billing;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BankAccountSortField implements SortField {
    NAME("name"),
    INITIAL_BALANCE("initialBalance"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
