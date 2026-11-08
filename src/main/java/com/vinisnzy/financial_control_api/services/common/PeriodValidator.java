package com.vinisnzy.financial_control_api.services.common;

import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;

import java.time.LocalDate;

public final class PeriodValidator {

    private PeriodValidator() {
    }

    public static boolean hasPeriod(LocalDate start, LocalDate end) {
        if ((start == null) != (end == null)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD, "'start' and 'end' must be sent together.");
        }
        if (start != null && start.isAfter(end)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD, "'start' must not be after 'end'.");
        }
        return start != null;
    }
}
