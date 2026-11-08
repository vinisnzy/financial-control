package com.vinisnzy.financial_control_api.controllers.auth;

import com.vinisnzy.financial_control_api.dtos.auth.RefreshRequest;
import com.vinisnzy.financial_control_api.dtos.auth.SignInRequest;
import com.vinisnzy.financial_control_api.dtos.auth.SignInResponse;
import com.vinisnzy.financial_control_api.dtos.auth.SignUpRequest;
import com.vinisnzy.financial_control_api.services.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest data) {
        this.service.signUp(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest data) {
        return ResponseEntity.ok(this.service.signIn(data));
    }

    @PostMapping("/refresh")
    public ResponseEntity<SignInResponse> refresh(@Valid @RequestBody RefreshRequest data) {
        return ResponseEntity.ok(this.service.refresh(data));
    }

    // TODO: Logout
}
