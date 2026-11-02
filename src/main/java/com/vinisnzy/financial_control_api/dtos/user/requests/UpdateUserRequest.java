package com.vinisnzy.financial_control_api.dtos.user.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Update a user.")
public record UpdateUserRequest(
        @Schema(description = "User email", example = "user@example.com", nullable = true)
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must have at most 255 characters.")
        String email
) {
}
