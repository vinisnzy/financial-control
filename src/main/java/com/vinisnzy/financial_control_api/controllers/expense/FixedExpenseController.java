package com.vinisnzy.financial_control_api.controllers.expense;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.expense.requests.CreateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.requests.UpdateFixedExpenseRequest;
import com.vinisnzy.financial_control_api.dtos.expense.responses.FixedExpenseResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.expense.FixedExpenseService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/fixed-expenses")
public class FixedExpenseController {

    private final FixedExpenseService service;

    public FixedExpenseController(FixedExpenseService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<FixedExpenseResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixedExpenseResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @GetMapping("/expense-category/{expenseCategoryId}")
    public ResponseEntity<PageResponse<FixedExpenseResponse>> getByExpenseCategory(
            @PathVariable UUID expenseCategoryId,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByExpenseCategory(expenseCategoryId, user.getId(), pagination));
    }

    @PostMapping
    public ResponseEntity<FixedExpenseResponse> create(
            @Valid @RequestBody CreateFixedExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FixedExpenseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFixedExpenseRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(id, data, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        this.service.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
