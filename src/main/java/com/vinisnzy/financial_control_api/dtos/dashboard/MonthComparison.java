package com.vinisnzy.financial_control_api.dtos.dashboard;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthComparison(YearMonth month, BigDecimal income, BigDecimal expense, BigDecimal balance) {
}
