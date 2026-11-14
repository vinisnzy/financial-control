package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateInstallmentExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.InstallmentExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.expense.InstallmentExpenseService;
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

@Tag(name = "Installment Expenses", description = "Installment purchase management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/installment-expenses")
public class InstallmentExpenseController {

    private final InstallmentExpenseService service;

    public InstallmentExpenseController(InstallmentExpenseService service) {
        this.service = service;
    }

    @Operation(
            summary = "List installment expenses",
            description = "Returns a page of the authenticated user's installment expenses.")
    @ApiResponse(responseCode = "200", description = "Installment expenses listed successfully")
    @GetMapping
    public ResponseEntity<PageResponse<InstallmentExpenseResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @Operation(summary = "Get an installment expense by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Installment expense found"),
            @ApiResponse(responseCode = "404", description = "Installment expense not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<InstallmentExpenseResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(
            summary = "Create an installment expense",
            description = "Creates the installment expense and generates one expense for each installment.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Installment expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Referenced expense category or payment method not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<InstallmentExpenseResponse> create(
            @Valid @RequestBody CreateInstallmentExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update an installment expense",
            description = "Partially updates the installment expense. Changing the total amount, the number of "
                    + "installments or the first due date regenerates its installments, which is not allowed "
                    + "once any of them is paid.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Installment expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Installment expense or referenced resource not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Schedule cannot change because there are paid installments", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<InstallmentExpenseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInstallmentExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete an installment expense")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Installment expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Installment expense not found", content = @Content)
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
