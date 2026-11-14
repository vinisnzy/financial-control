package com.vinisnzy.financial_control_api.dtos.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryShare(
        UUID categoryId,
        String categoryName,
        BigDecimal total,
        BigDecimal percentage
) {
}
