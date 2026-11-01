package com.vinisnzy.financial_control_api.dtos.billing.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Create a bank account.")
public record CreateBankAccountRequest(
        @Schema(description = "Bank account name", example = "Nubank Checking")
        @NotBlank(message = "Name cannot be empty.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Initial balance", example = "1500.00")
        @NotNull(message = "Initial balance cannot be empty.")
        @DecimalMin(value = "0.00", message = "Initial balance must be at least 0.00.")
        @Digits(integer = 12, fraction = 2, message = "Initial balance must have at most 12 integer digits and 2 decimal places.")
        BigDecimal initialBalance
) {}
