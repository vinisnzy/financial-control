package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.FixedExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.expense.FixedExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Fixed Expenses", description = "Recurring expense management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/fixed-expenses")
public class FixedExpenseController {

    private final FixedExpenseService service;

    public FixedExpenseController(FixedExpenseService service) {
        this.service = service;
    }

    @Operation(
            summary = "List fixed expenses",
            description = "Returns a page of the authenticated user's fixed expenses.")
    @ApiResponse(responseCode = "200", description = "Fixed expenses listed successfully")
    @GetMapping
    public ResponseEntity<PageResponse<FixedExpenseResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @Operation(summary = "Get a fixed expense by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fixed expense found"),
            @ApiResponse(responseCode = "404", description = "Fixed expense not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<FixedExpenseResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(
            summary = "List fixed expenses by category",
            description = "Returns a page of the fixed expenses that belong to the informed expense category.")
    @ApiResponse(responseCode = "200", description = "Fixed expenses listed successfully")
    @GetMapping("/expense-category/{expenseCategoryId}")
    public ResponseEntity<PageResponse<FixedExpenseResponse>> getByExpenseCategory(
            @PathVariable UUID expenseCategoryId,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByExpenseCategory(expenseCategoryId, user.getId(), pagination));
    }

    @Operation(summary = "Create a fixed expense")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fixed expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Referenced expense category not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<FixedExpenseResponse> create(
            @Valid @RequestBody CreateFixedExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update a fixed expense",
            description = "Partially updates the fixed expense. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fixed expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fixed expense or referenced expense category not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<FixedExpenseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFixedExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete a fixed expense")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fixed expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Fixed expense not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        this.service.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
