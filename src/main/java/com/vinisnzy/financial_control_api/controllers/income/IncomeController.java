package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.income.IncomeService;
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

@RestController
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService service;

    public IncomeController(IncomeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<IncomeResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(start, end, user.getId(), pagination));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<IncomeResponse>> getByName(
            @RequestParam String name,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByName(name, user.getId(), pagination));
    }

    @GetMapping("/bank-account/{bankAccountId}")
    public ResponseEntity<PageResponse<IncomeResponse>> getByBankAccount(
            @PathVariable UUID bankAccountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByBankAccount(bankAccountId, start, end, user.getId(), pagination));
    }

    @GetMapping("/income-category/{incomeCategoryId}")
    public ResponseEntity<PageResponse<IncomeResponse>> getByIncomeCategory(
            @PathVariable UUID incomeCategoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getByIncomeCategory(incomeCategoryId, start, end, user.getId(), pagination));
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> create(
            @Valid @RequestBody CreateIncomeRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(user.getId(), data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncomeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIncomeRequest data,
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
