package com.vinisnzy.financial_control_api.pagination;

import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;

public final class SortValidator {

    private SortValidator() {
    }

    public static Pageable validate(Pageable pageable, Collection<String> allowed, String defaultSort) {
        if (pageable.isUnpaged()) {
            return pageable;
        }

        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER,
                        "Invalid sort property '%s'. Allowed: %s.".formatted(order.getProperty(), String.join(", ", allowed)));
            }
        }

        if (pageable.getSort().isUnsorted() && defaultSort != null && !defaultSort.isBlank()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), parse(defaultSort, allowed));
        }
        return pageable;
    }

    private static Sort parse(String defaultSort, Collection<String> allowed) {
        String[] parts = defaultSort.split(",");
        String property = parts[0].trim();
        if (!allowed.contains(property)) {
            throw new IllegalStateException("Default sort '%s' is not in the whitelist %s.".formatted(property, allowed));
        }
        Sort.Direction direction = parts.length > 1 ? Sort.Direction.fromString(parts[1].trim()) : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
