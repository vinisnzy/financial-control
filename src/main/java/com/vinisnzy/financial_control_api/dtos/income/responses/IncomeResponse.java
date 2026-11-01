package com.vinisnzy.financial_control_api.dtos.income.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Return an income.")
public record IncomeResponse(
        @Schema(description = "Income id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Income name", example = "Freelance project")
        String name,

        @Schema(description = "Amount", example = "3000.00")
        BigDecimal amount,

        @Schema(description = "Id of the income category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID incomeCategoryId,

        @Schema(description = "Id of the bank account that receives the income", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID bankAccountId,

        @Schema(description = "Expected date", example = "2026-02-05")
        LocalDate expectedAt,

        @Schema(description = "Received date", example = "2026-02-05")
        LocalDate receivedAt,

        @Schema(description = "Id of the fixed income that generated this income", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID fixedIncomeId,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {}
