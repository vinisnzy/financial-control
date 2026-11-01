package com.vinisnzy.financial_control_api.dtos.billing.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Update a payment method.")
public record UpdatePaymentMethodRequest(
        @Schema(description = "Payment method name", example = "Nubank Credit Card", nullable = true)
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Payment method type", example = "CREDIT_CARD", nullable = true)
        @Pattern(regexp = ".*\\S.*", message = "Type cannot be blank.")
        @Size(max = 100, message = "Type must have at most 100 characters.")
        String type,

        @Schema(description = "Id of the bank account linked to this payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID bankAccountId
) {}
