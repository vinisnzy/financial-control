package com.vinisnzy.financial_control_api.controllers.auth;

import com.vinisnzy.financial_control_api.dtos.auth.*;
import com.vinisnzy.financial_control_api.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Authentication endpoints")
@SecurityRequirements
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(
            summary = "Sign up a user",
            description = "Creates a new user with email and password.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "409", description = "User with same email already exists", content = @Content)
    })
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest data) {
        this.service.signUp(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Sign in a user",
            description = "Authenticates with email and password and returns an access token and a refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest data) {
        return ResponseEntity.ok(this.service.signIn(data));
    }

    @Operation(
            summary = "Refresh a user's tokens",
            description = "Exchanges a valid refresh token for a new access token and a new refresh token. "
                    + "The informed refresh token is revoked. Reusing an already revoked refresh token "
                    + "revokes the whole session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "401", description = "Refresh token invalid, expired or reused", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<SignInResponse> refresh(@Valid @RequestBody RefreshRequest data) {
        return ResponseEntity.ok(this.service.refresh(data));
    }

    @Operation(
            summary = "Log out a user",
            description = "Revokes all refresh tokens of the session the informed refresh token belongs to. "
                    + "Unknown refresh token also returns 204."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest data) {
        this.service.logout(data);
        return ResponseEntity.noContent().build();
    }
}
