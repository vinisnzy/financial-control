package com.vinisnzy.financial_control_api.dtos.income.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Update an income.")
public record UpdateIncomeRequest(
        @Schema(description = "Income name", example = "Freelance project", nullable = true)
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Amount", example = "3000.00", nullable = true)
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01.")
        @Digits(integer = 12, fraction = 2, message = "Amount must have at most 12 integer digits and 2 decimal places.")
        BigDecimal amount,

        @Schema(description = "Id of the income category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID incomeCategoryId,

        @Schema(description = "Id of the bank account that receives the income", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID bankAccountId,

        @Schema(description = "Expected date", example = "2026-02-05", nullable = true)
        LocalDate expectedAt,

        @Schema(description = "Received date", example = "2026-02-05", nullable = true)
        @PastOrPresent(message = "Received date cannot be in the future.")
        LocalDate receivedAt
) {}
