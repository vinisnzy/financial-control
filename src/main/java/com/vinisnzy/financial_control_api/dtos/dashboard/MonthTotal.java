package com.vinisnzy.financial_control_api.dtos.dashboard;

import java.math.BigDecimal;

public record MonthTotal(int year, int month, BigDecimal total) {
}
