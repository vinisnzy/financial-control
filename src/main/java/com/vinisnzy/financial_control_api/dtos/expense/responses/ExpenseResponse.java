package com.vinisnzy.financial_control_api.dtos.expense.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Return an expense.")
public record ExpenseResponse(
        @Schema(description = "Expense id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Expense name", example = "Supermarket")
        String name,

        @Schema(description = "Amount", example = "250.90")
        BigDecimal amount,

        @Schema(description = "Id of the expense category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID expenseCategoryId,

        @Schema(description = "Id of the payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID paymentMethodId,

        @Schema(description = "Due date", example = "2026-02-10")
        LocalDate dueDate,

        @Schema(description = "Payment date", example = "2026-02-09")
        LocalDate paidAt,

        @Schema(description = "Id of the fixed expense that generated this expense", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID fixedExpenseId,

        @Schema(description = "Id of the installment expense that generated this expense", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID installmentExpenseId,

        @Schema(description = "Installment number, when part of an installment expense", example = "1", nullable = true)
        Short installmentNumber,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {}
