package com.example.order_management.controller;

import com.example.order_management.dto.response.UserResponse;
import com.example.order_management.entity.User;
import com.example.order_management.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Controller.
 *
 * Exposes endpoints for authenticated user profile operations.
 * Protected by Spring Security and JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user profile and account management")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @param userDetails injected automatically by Spring Security from the verified JWT
     * @return UserResponse containing user details
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the profile information of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}

