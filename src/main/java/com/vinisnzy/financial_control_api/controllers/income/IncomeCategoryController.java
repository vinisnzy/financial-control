package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateIncomeCategoryRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.IncomeCategoryResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.income.IncomeCategoryService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/income-categories")
public class IncomeCategoryController {

    private final IncomeCategoryService service;

    public IncomeCategoryController(IncomeCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<IncomeCategoryResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeCategoryResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<IncomeCategoryResponse> create(
            @Valid @RequestBody CreateIncomeCategoryRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncomeCategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIncomeCategoryRequest data,
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
