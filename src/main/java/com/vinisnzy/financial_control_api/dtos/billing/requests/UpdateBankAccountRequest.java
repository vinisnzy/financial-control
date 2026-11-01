package com.vinisnzy.financial_control_api.dtos.billing.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Update a bank account.")
public record UpdateBankAccountRequest(
        @Schema(description = "Bank account name", example = "Nubank Checking", nullable = true)
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Initial balance", example = "1500.00", nullable = true)
        @DecimalMin(value = "0.00", message = "Initial balance must be at least 0.00.")
        @Digits(integer = 12, fraction = 2, message = "Initial balance must have at most 12 integer digits and 2 decimal places.")
        BigDecimal initialBalance
) {}
