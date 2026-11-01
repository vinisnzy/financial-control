package com.vinisnzy.financial_control_api.dtos.expense.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Update an installment expense.")
public record UpdateInstallmentExpenseRequest(
        @Schema(description = "Installment expense name", example = "New Laptop", nullable = true)
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Total amount", example = "4800.00", nullable = true)
        @DecimalMin(value = "0.01", message = "Total amount must be at least 0.01.")
        @Digits(integer = 12, fraction = 2, message = "Total amount must have at most 12 integer digits and 2 decimal places.")
        BigDecimal totalAmount,

        @Schema(description = "Number of installments", example = "12", nullable = true)
        @Min(value = 2, message = "Installments must be at least 2.")
        @Max(value = 360, message = "Installments must be at most 360.")
        Short installments,

        @Schema(description = "Id of the expense category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID expenseCategoryId,

        @Schema(description = "Id of the payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID paymentMethodId,

        @Schema(description = "First due date", example = "2026-02-10", nullable = true)
        LocalDate firstDueDate
) {}
