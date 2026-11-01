package com.vinisnzy.financial_control_api.dtos.income.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Create an income.")
public record CreateIncomeRequest(
        @Schema(description = "Income name", example = "Freelance project")
        @NotBlank(message = "Name cannot be empty.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Amount", example = "3000.00")
        @NotNull(message = "Amount cannot be empty.")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01.")
        @Digits(integer = 12, fraction = 2, message = "Amount must have at most 12 integer digits and 2 decimal places.")
        BigDecimal amount,

        @Schema(description = "Id of the income category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Income category id cannot be empty.")
        UUID incomeCategoryId,

        @Schema(description = "Id of the bank account that receives the income", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Bank account that receives the income id cannot be empty.")
        UUID bankAccountId,

        @Schema(description = "Expected date", example = "2026-02-05")
        @NotNull(message = "Expected date cannot be empty.")
        LocalDate expectedAt,

        @Schema(description = "Received date", example = "2026-02-05")
        @PastOrPresent(message = "Received date cannot be in the future.")
        LocalDate receivedAt
) {}
