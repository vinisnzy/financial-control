package com.vinisnzy.financial_control_api.controllers.billing;

import com.vinisnzy.financial_control_api.pagination.SortField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentMethodSortField implements SortField {
    NAME("name"),
    TYPE("type"),
    CREATED_AT("createdAt");

    private final String property;

    @Override
    public String property() {
        return property;
    }
}
