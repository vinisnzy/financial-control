package com.vinisnzy.financial_control_api.dtos.user.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Create a user.")
public record CreateUserRequest(
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "Email cannot be empty.")
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must have at most 255 characters.")
        String email,

        @Schema(description = "User password", example = "S3cretPass!", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "Password cannot be empty.")
        @Size(min = 8, max = 72, message = "Password must have between 8 and 72 characters.")
        String password
) {}
