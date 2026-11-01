package com.vinisnzy.financial_control_api.dtos.income.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Return a fixed income.")
public record FixedIncomeResponse(
        @Schema(description = "Fixed income id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Fixed income name", example = "Monthly salary")
        String name,

        @Schema(description = "Amount", example = "5000.00")
        BigDecimal amount,

        @Schema(description = "Id of the income category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID incomeCategoryId,

        @Schema(description = "Id of the bank account that receives the income", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID bankAccountId,

        @Schema(description = "Payday (day of the month, 1-31)", example = "5")
        Short payday,

        @Schema(description = "Start date", example = "2026-01-01")
        LocalDate startDate,

        @Schema(description = "End date", example = "2026-12-31")
        LocalDate endDate,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {}
