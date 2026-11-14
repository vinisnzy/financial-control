package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeCategoryResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.income.IncomeCategoryService;
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

@Tag(name = "Income Categories", description = "Income category management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/income-categories")
public class IncomeCategoryController {

    private final IncomeCategoryService service;

    public IncomeCategoryController(IncomeCategoryService service) {
        this.service = service;
    }

    @Operation(
            summary = "List income categories",
            description = "Returns a page with the default categories and the authenticated user's own categories.")
    @ApiResponse(responseCode = "200", description = "Income categories listed successfully")
    @GetMapping
    public ResponseEntity<PageResponse<IncomeCategoryResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @Operation(summary = "Get an income category by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Income category found"),
            @ApiResponse(responseCode = "404", description = "Income category not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<IncomeCategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(summary = "Create an income category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Income category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    })
    @PostMapping
    public ResponseEntity<IncomeCategoryResponse> create(
            @Valid @RequestBody CreateIncomeCategoryRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update an income category",
            description = "Partially updates the category. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Income category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Income category not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<IncomeCategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIncomeCategoryRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete an income category")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Income category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Income category not found", content = @Content)
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
