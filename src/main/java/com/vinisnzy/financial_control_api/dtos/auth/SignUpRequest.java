package com.vinisnzy.financial_control_api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Sign up.")
public record SignUpRequest(
        @Schema(description = "User email", example = "user@email.com")
        @Email(message = "Email must be a valid email.")
        @NotBlank(message = "Email cannot be empty.")
        String email,

        @Schema(description = "User password", example = "@Us3rP4ssw0rd1")
        @Size(min = 8, message = "Password must have at least 8 characters.")
        @NotBlank(message = "Password cannot be empty.")
        String password
) {
}
