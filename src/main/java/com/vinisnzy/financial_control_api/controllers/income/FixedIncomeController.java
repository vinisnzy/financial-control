package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.FixedIncomeResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.income.FixedIncomeService;
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

@Tag(name = "Fixed Incomes", description = "Recurring income management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/fixed-incomes")
public class FixedIncomeController {

    private final FixedIncomeService service;

    public FixedIncomeController(FixedIncomeService service) {
        this.service = service;
    }

    @Operation(
            summary = "List fixed incomes",
            description = "Returns a page of the authenticated user's fixed incomes.")
    @ApiResponse(responseCode = "200", description = "Fixed incomes listed successfully")
    @GetMapping
    public ResponseEntity<PageResponse<FixedIncomeResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @Operation(summary = "Get a fixed income by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fixed income found"),
            @ApiResponse(responseCode = "404", description = "Fixed income not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<FixedIncomeResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(summary = "Create a fixed income")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fixed income created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Referenced income category not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<FixedIncomeResponse> create(
            @Valid @RequestBody CreateFixedIncomeRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update a fixed income",
            description = "Partially updates the fixed income. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fixed income updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fixed income or referenced income category not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<FixedIncomeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFixedIncomeRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete a fixed income")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fixed income deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Fixed income not found", content = @Content)
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
