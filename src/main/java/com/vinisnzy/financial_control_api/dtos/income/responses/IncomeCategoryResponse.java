package com.vinisnzy.financial_control_api.dtos.income.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Return an income category.")
public record IncomeCategoryResponse(
        @Schema(description = "Income category id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Income category name", example = "Salary")
        String name,

        @Schema(description = "Category color as a hex code", example = "#FF5733")
        String color,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {}
