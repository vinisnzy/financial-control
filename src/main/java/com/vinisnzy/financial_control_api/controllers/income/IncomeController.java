package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.income.IncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.vinisnzy.financial_control_api.pagination.SortableBy;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Incomes", description = "Income management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService service;

    public IncomeController(IncomeService service) {
        this.service = service;
    }

    @Operation(
            summary = "List incomes",
            description = "Returns a page of the authenticated user's incomes, optionally filtered by period. "
                    + "'start' and 'end' must be sent together.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incomes listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid period", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponse<IncomeResponse>> getAll(
            @Parameter(description = "Period start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject @SortableBy(IncomeSortField.class) Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(start, end, user.getId(), pagination));
    }

    @Operation(summary = "Get an income by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Income found"),
            @ApiResponse(responseCode = "404", description = "Income not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(
            summary = "Search incomes by name",
            description = "Returns a page of the incomes whose name matches the informed text.")
    @ApiResponse(responseCode = "200", description = "Incomes listed successfully")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<IncomeResponse>> getByName(
            @Parameter(description = "Text to search in the income name")
            @RequestParam String name,
            @ParameterObject @SortableBy(IncomeSortField.class) Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByName(name, user.getId(), pagination));
    }

    @Operation(
            summary = "List incomes by bank account",
            description = "Returns a page of the incomes received in the informed bank account, optionally "
                    + "filtered by period. 'start' and 'end' must be sent together.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incomes listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid period", content = @Content)
    })
    @GetMapping("/bank-account/{bankAccountId}")
    public ResponseEntity<PageResponse<IncomeResponse>> getByBankAccount(
            @PathVariable UUID bankAccountId,
            @Parameter(description = "Period start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject @SortableBy(IncomeSortField.class) Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByBankAccount(bankAccountId, start, end, user.getId(), pagination));
    }

    @Operation(
            summary = "List incomes by category",
            description = "Returns a page of the incomes of the informed category, optionally filtered by period. "
                    + "'start' and 'end' must be sent together.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incomes listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid period", content = @Content)
    })
    @GetMapping("/income-category/{incomeCategoryId}")
    public ResponseEntity<PageResponse<IncomeResponse>> getByIncomeCategory(
            @PathVariable UUID incomeCategoryId,
            @Parameter(description = "Period start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject @SortableBy(IncomeSortField.class) Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByIncomeCategory(incomeCategoryId, start, end, user.getId(), pagination));
    }

    @Operation(summary = "Create an income")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Income created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Referenced resource not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<IncomeResponse> create(
            @Valid @RequestBody CreateIncomeRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(user.getId(), data));
    }

    @Operation(
            summary = "Update an income",
            description = "Partially updates the income. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Income updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Income or referenced resource not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<IncomeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIncomeRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete an income")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Income deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Income not found", content = @Content)
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
