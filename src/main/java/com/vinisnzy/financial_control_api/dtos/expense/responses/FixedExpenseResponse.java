package com.vinisnzy.financial_control_api.dtos.expense.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Return a fixed expense.")
public record FixedExpenseResponse(
        @Schema(description = "Fixed expense id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Fixed expense name", example = "Rent")
        String name,

        @Schema(description = "Amount", example = "1200.00")
        BigDecimal amount,

        @Schema(description = "Id of the expense category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID expenseCategoryId,

        @Schema(description = "Id of the payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID paymentMethodId,

        @Schema(description = "Due day (day of the month, 1-31)", example = "10")
        Short dueDay,

        @Schema(description = "Start date", example = "2026-01-01")
        LocalDate startDate,

        @Schema(description = "End date", example = "2026-12-31")
        LocalDate endDate,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {}
