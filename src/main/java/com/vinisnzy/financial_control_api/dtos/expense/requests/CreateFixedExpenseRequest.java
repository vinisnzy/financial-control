package com.vinisnzy.financial_control_api.dtos.expense.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Create a fixed expense.")
public record CreateFixedExpenseRequest(
        @Schema(description = "Fixed expense name", example = "Rent")
        @NotBlank(message = "Name cannot be empty.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Amount", example = "1200.00")
        @NotNull(message = "Amount cannot be empty.")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01.")
        @Digits(integer = 12, fraction = 2, message = "Amount must have at most 12 integer digits and 2 decimal places.")
        BigDecimal amount,

        @Schema(description = "Id of the expense category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Expense category id cannot be empty.")
        UUID expenseCategoryId,

        @Schema(description = "Id of the payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Payment method id cannot be empty.")
        UUID paymentMethodId,

        @Schema(description = "Due day (day of the month, 1-31)", example = "10")
        @NotNull(message = "Due day cannot be empty.")
        @Min(value = 1, message = "Due day must be between 1 and 31.")
        @Max(value = 31, message = "Due day must be between 1 and 31.")
        Short dueDay,

        @Schema(description = "Start date", example = "2026-01-01")
        @NotNull(message = "Start date cannot be empty.")
        LocalDate startDate,

        @Schema(description = "End date", example = "2026-12-31")
        LocalDate endDate
) {
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "End date must be on or after start date.")
    public boolean isEndDateValid() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }
}
