package com.vinisnzy.financial_control_api.dtos.dashboard;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummaryResponse(
        YearMonth month,
        BigDecimal plannedIncome,
        BigDecimal receivedIncome,
        BigDecimal plannedExpense,
        BigDecimal paidExpense,
        BigDecimal plannedBalance,
        BigDecimal realizedBalance
) {
}
