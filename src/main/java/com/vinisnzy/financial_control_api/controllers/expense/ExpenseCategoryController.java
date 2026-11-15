package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseCategoryResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.expense.ExpenseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.vinisnzy.financial_control_api.pagination.SortableBy;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Expense Categories", description = "Expense category management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/expense-categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService service;

    public ExpenseCategoryController(ExpenseCategoryService service) {
        this.service = service;
    }

    @Operation(
            summary = "List expense categories",
            description = "Returns a page with the default categories and the authenticated user's own categories.")
    @ApiResponse(responseCode = "200", description = "Expense categories listed successfully")
    @GetMapping
    public ResponseEntity<PageResponse<ExpenseCategoryResponse>> getAll(
            @ParameterObject @SortableBy(ExpenseCategorySortField.class) Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @Operation(summary = "Get an expense category by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense category found"),
            @ApiResponse(responseCode = "404", description = "Expense category not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseCategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(summary = "Create an expense category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Expense category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ExpenseCategoryResponse> create(
            @Valid @RequestBody CreateExpenseCategoryRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update an expense category",
            description = "Partially updates the category. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Expense category not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseCategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExpenseCategoryRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete an expense category")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Expense category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense category not found", content = @Content)
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
