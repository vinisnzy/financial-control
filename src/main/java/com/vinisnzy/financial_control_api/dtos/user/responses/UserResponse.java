package com.vinisnzy.financial_control_api.dtos.user.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Return a user.")
public record UserResponse(
        @Schema(description = "User id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "User email", example = "user@example.com")
        String email,

        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-20T08:00:00Z", nullable = true)
        Instant updatedAt
) {}
