package com.vinisnzy.financial_control_api.controllers.auth;

import com.vinisnzy.financial_control_api.dtos.user.requests.UpdateUserRequest;
import com.vinisnzy.financial_control_api.dtos.user.responses.UserResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(this.service.getById(user.getId()));
    }

    @PatchMapping
    public ResponseEntity<UserResponse> update(
            @Valid @RequestBody UpdateUserRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(user.getId(), data));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user) {
        this.service.delete(user.getId());
        return ResponseEntity.noContent().build();
    }
}
