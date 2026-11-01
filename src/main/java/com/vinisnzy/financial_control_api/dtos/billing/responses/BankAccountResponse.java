package com.vinisnzy.financial_control_api.dtos.billing.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Return a bank account.")
public record BankAccountResponse(
        @Schema(description = "Bank account id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Bank account name", example = "Nubank Checking")
        String name,

        @Schema(description = "Initial balance", example = "1500.00")
        BigDecimal initialBalance,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {}
