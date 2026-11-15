package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FixedIncomeSortField implements SortField {
    NAME("name"),
    AMOUNT("amount"),
    PAYDAY("payday"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
