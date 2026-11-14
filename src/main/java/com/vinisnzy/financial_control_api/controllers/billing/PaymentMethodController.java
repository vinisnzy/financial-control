package com.vinisnzy.financial_control_api.controllers.billing;

import com.vinisnzy.financial_control_api.dtos.billing.requests.CreatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.requests.UpdatePaymentMethodRequest;
import com.vinisnzy.financial_control_api.dtos.billing.responses.PaymentMethodResponse;
import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.billing.PaymentMethodService;
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

@Tag(name = "Payment Methods", description = "Payment method management")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService service;

    public PaymentMethodController(PaymentMethodService service) {
        this.service = service;
    }

    @Operation(
            summary = "List payment methods",
            description = "Returns a page of the authenticated user's payment methods.")
    @ApiResponse(responseCode = "200", description = "Payment methods listed successfully")
    @GetMapping
    public ResponseEntity<PageResponse<PaymentMethodResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @Operation(summary = "Get a payment method by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment method found"),
            @ApiResponse(responseCode = "404", description = "Payment method not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @Operation(
            summary = "List payment methods by bank account",
            description = "Returns a page of the payment methods linked to the informed bank account.")
    @ApiResponse(responseCode = "200", description = "Payment methods listed successfully")
    @GetMapping("/bank-account/{bankAccountId}")
    public ResponseEntity<PageResponse<PaymentMethodResponse>> getByBankAccountId(
            @PathVariable UUID bankAccountId,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByBankAccountId(user.getId(), bankAccountId, pagination));
    }

    @Operation(summary = "Create a payment method")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment method created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Referenced bank account not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PaymentMethodResponse> create(
            @Valid @RequestBody CreatePaymentMethodRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @Operation(
            summary = "Update a payment method",
            description = "Partially updates the payment method. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment method updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Payment method or referenced bank account not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentMethodRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @Operation(summary = "Delete a payment method")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Payment method deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Payment method not found", content = @Content)
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
