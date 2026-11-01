package com.vinisnzy.financial_control_api.dtos.income.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Create an income category.")
public record CreateIncomeCategoryRequest(
        @Schema(description = "Income category name", example = "Salary")
        @NotBlank(message = "Name cannot be empty.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @Schema(description = "Category color as a hex code", example = "#FF5733")
        @NotBlank(message = "Color cannot be empty.")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex code like #FF5733.")
        String color
) {}
