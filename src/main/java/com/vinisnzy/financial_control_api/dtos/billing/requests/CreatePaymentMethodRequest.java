package com.vinisnzy.financial_control_api.dtos.billing.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Create a payment method.")
public record CreatePaymentMethodRequest(
        @Schema(description = "Payment method name", example = "Nubank Credit Card")
        @NotBlank(message = "Name cannot be empty.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Payment method type", example = "CREDIT_CARD")
        @NotBlank(message = "Type cannot be empty.")
        @Size(max = 100, message = "Type must have at most 100 characters.")
        String type,

        @Schema(description = "Id of the bank account linked to this payment method", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Bank account linked to this payment method id cannot be empty.")
        UUID bankAccountId
) {}
