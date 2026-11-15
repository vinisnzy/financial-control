package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IncomeSortField implements SortField {
    NAME("name"),
    AMOUNT("amount"),
    EXPECTED_AT("expectedAt"),
    RECEIVED_AT("receivedAt"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
