package com.vinisnzy.financial_control_api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh.")
public record RefreshRequest(
        @Schema(description = "Refresh token", example = "your-refresh-token")
        @NotBlank(message = "Refresh token cannot be empty.")
        String refreshToken
) {
}
