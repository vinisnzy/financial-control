package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.ExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.expense.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Expenses", description = "Expense management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @Operation(
            summary = "List expenses",
            description = "Returns a page of the authenticated user's expenses, optionally filtered by period. "
                    + "'start' and 'end' must be sent together.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expenses listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid period", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageResponse<ExpenseResponse>> getAll(
            @Parameter(description = "Period start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(start, end, user.getId(), pagination));
    }

    @Operation(summary = "Get an expense by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense found"),
            @ApiResponse(responseCode = "404", description = "Expense not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(
            summary = "Search expenses by name",
            description = "Returns a page of the expenses whose name matches the informed text.")
    @ApiResponse(responseCode = "200", description = "Expenses listed successfully")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ExpenseResponse>> getByName(
            @Parameter(description = "Text to search in the expense name")
            @RequestParam String name,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByName(name, user.getId(), pagination));
    }

    @Operation(
            summary = "List expenses by category",
            description = "Returns a page of the expenses of the informed category, optionally filtered by period. "
                    + "'start' and 'end' must be sent together.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expenses listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid period", content = @Content)
    })
    @GetMapping("/expense-category/{expenseCategoryId}")
    public ResponseEntity<PageResponse<ExpenseResponse>> getByExpenseCategory(
            @PathVariable UUID expenseCategoryId,
            @Parameter(description = "Period start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByExpenseCategory(expenseCategoryId, start, end, user.getId(), pagination));
    }

    @Operation(
            summary = "List expenses by payment method",
            description = "Returns a page of the expenses paid with the informed payment method, optionally "
                    + "filtered by period. 'start' and 'end' must be sent together.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expenses listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid period", content = @Content)
    })
    @GetMapping("/payment-method/{paymentMethodId}")
    public ResponseEntity<PageResponse<ExpenseResponse>> getByPaymentMethod(
            @PathVariable UUID paymentMethodId,
            @Parameter(description = "Period start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "Period end date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByPaymentMethod(paymentMethodId, start, end, user.getId(), pagination));
    }

    @Operation(
            summary = "List expenses by installment expense",
            description = "Returns a page with the installments of the informed installment expense.")
    @ApiResponse(responseCode = "200", description = "Expenses listed successfully")
    @GetMapping("/installment-expense/{installmentExpenseId}")
    public ResponseEntity<PageResponse<ExpenseResponse>> getByInstallmentExpense(
            @PathVariable UUID installmentExpenseId,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByInstallmentExpense(installmentExpenseId, user.getId(), pagination));
    }

    @Operation(summary = "Create an expense")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Referenced resource not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody CreateExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update an expense",
            description = "Partially updates the expense. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Expense or referenced resource not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete an expense")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found", content = @Content)
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
