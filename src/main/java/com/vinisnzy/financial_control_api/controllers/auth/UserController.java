package com.vinisnzy.financial_control_api.controllers.auth;

import com.vinisnzy.financial_control_api.dtos.user.requests.UpdateUserRequest;
import com.vinisnzy.financial_control_api.dtos.user.responses.UserResponse;
import com.vinisnzy.financial_control_api.entities.auth.User;
import com.vinisnzy.financial_control_api.services.auth.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Authenticated user profile")
@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
@RestController
@RequestMapping("/users/me")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(
            summary = "Get the authenticated user",
            description = "Returns the profile of the user that owns the access token.")
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(this.service.getById(user.getId()));
    }

    @Operation(
            summary = "Update the authenticated user",
            description = "Partially updates the profile. Only the informed fields are changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    })
    @PatchMapping
    public ResponseEntity<UserResponse> update(
            @Valid @RequestBody UpdateUserRequest data,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(this.service.update(user.getId(), data));
    }

    @Operation(
            summary = "Delete the authenticated user",
            description = "Deletes the account of the user that owns the access token.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user) {
        this.service.delete(user.getId());
        return ResponseEntity.noContent().build();
    }
}
