package com.vinisnzy.financial_control_api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Sign in.")
public record SignInRequest(
        @Schema(description = "User email", example = "user@email.com")
        @NotBlank(message = "Email cannot be empty.")
        String email,
        @Schema(description = "User password", example = "@Us3rP4ssw0rd1")
        @NotBlank(message = "Password cannot be empty.")
        String password
) {
}
