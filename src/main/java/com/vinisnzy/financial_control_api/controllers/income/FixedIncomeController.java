package com.vinisnzy.financial_control_api.controllers.income;

import com.vinisnzy.financial_control_api.dtos.common.PageResponse;
import com.vinisnzy.financial_control_api.dtos.income.requests.CreateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.requests.UpdateFixedIncomeRequest;
import com.vinisnzy.financial_control_api.dtos.income.responses.FixedIncomeResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.income.FixedIncomeService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/fixed-incomes")
public class FixedIncomeController {

    private final FixedIncomeService service;

    public FixedIncomeController(FixedIncomeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<FixedIncomeResponse>> getAll(
            @ParameterObject Pageable pagination,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getAll(user.getId(), pagination));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixedIncomeResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.getById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<FixedIncomeResponse> create(
            @Valid @RequestBody CreateFixedIncomeRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(data, user.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FixedIncomeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFixedIncomeRequest data,
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
