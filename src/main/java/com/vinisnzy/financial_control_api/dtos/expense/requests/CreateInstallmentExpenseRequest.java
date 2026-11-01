package com.vinisnzy.financial_control_api.dtos.expense.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Create an installment expense.")
public record CreateInstallmentExpenseRequest(
        @Schema(description = "Installment expense name", example = "New Laptop")
        @NotBlank(message = "Name cannot be empty.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Total amount", example = "4800.00")
        @NotNull(message = "Total amount cannot be empty.")
        @DecimalMin(value = "0.01", message = "Total amount must be at least 0.01.")
        @Digits(integer = 12, fraction = 2, message = "Total amount must have at most 12 integer digits and 2 decimal places.")
        BigDecimal totalAmount,

        @Schema(description = "Number of installments", example = "12")
        @NotNull(message = "Installments cannot be empty.")
        @Min(value = 2, message = "Installments must be at least 2.")
        @Max(value = 360, message = "Installments must be at most 360.")
        Short installments,

        @Schema(description = "Id of the expense category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Expense category id cannot be empty.")
        UUID expenseCategoryId,

        @Schema(description = "Id of the payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Payment method id cannot be empty.")
        UUID paymentMethodId,

        @Schema(description = "First due date", example = "2026-02-10")
        @NotNull(message = "First due date cannot be empty.")
        LocalDate firstDueDate
) {}
