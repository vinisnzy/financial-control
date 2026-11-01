package com.vinisnzy.financial_control_api.dtos.expense.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Update an expense.")
public record UpdateExpenseRequest(
        @Schema(description = "Expense name", example = "Supermarket", nullable = true)
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Amount", example = "250.90", nullable = true)
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01.")
        @Digits(integer = 12, fraction = 2, message = "Amount must have at most 12 integer digits and 2 decimal places.")
        BigDecimal amount,

        @Schema(description = "Id of the expense category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID expenseCategoryId,

        @Schema(description = "Id of the payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID paymentMethodId,

        @Schema(description = "Due date", example = "2026-02-10", nullable = true)
        LocalDate dueDate,

        @Schema(description = "Payment date", example = "2026-02-09", nullable = true)
        @PastOrPresent(message = "Payment date cannot be in the future.")
        LocalDate paidAt
) {}
