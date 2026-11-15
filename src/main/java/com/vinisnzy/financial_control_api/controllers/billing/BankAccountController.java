package com.vinisnzy.financial_control_api.controllers.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdateBankAccountRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.BankAccountResponse;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.billing.BankAccountService;
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

@Tag(name = "Bank Accounts", description = "Bank account management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/bank-accounts")
public class BankAccountController {

    private final BankAccountService service;

    public BankAccountController(BankAccountService service) {
        this.service = service;
    }

    @Operation(
            summary = "List bank accounts",
            description = "Returns a page of the authenticated user's bank accounts.")
    @ApiResponse(responseCode = "200", description = "Bank accounts listed successfully")
    @GetMapping
    public ResponseEntity<PageResponse<BankAccountResponse>> getAll(
            @ParameterObject @SortableBy(BankAccountSortField.class) Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @Operation(summary = "Get a bank account by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank account found"),
            @ApiResponse(responseCode = "404", description = "Bank account not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(summary = "Create a bank account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bank account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BankAccountResponse> create(
            @Valid @RequestBody CreateBankAccountRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update a bank account",
            description = "Partially updates the bank account. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bank account not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<BankAccountResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankAccountRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete a bank account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bank account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found", content = @Content)
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
