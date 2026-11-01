package com.vinisnzy.financial_control_api.dtos.expense.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Return an installment expense.")
public record InstallmentExpenseResponse(
        @Schema(description = "Installment expense id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Installment expense name", example = "New Laptop")
        String name,

        @Schema(description = "Total amount", example = "4800.00")
        BigDecimal totalAmount,

        @Schema(description = "Number of installments", example = "12")
        Short installments,

        @Schema(description = "Id of the expense category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID expenseCategoryId,

        @Schema(description = "Id of the payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID paymentMethodId,

        @Schema(description = "First due date", example = "2026-02-10")
        LocalDate firstDueDate,

        @Schema(description = "Installments by this installment expense")
        List<ExpenseResponse> expenses,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {
}
