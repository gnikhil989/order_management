package com.example.order_management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login Request Data Transfer Object (DTO).
 *
 * Captures user credentials for authentication.
 */
@Schema(description = "Payload for authenticating and obtaining a JWT token")
public record LoginRequest(

        @Schema(description = "Registered email address", example = "john.doe@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Please provide a valid email format")
        String email,

        @Schema(description = "Account password", example = "Secret123!")
        @NotBlank(message = "Password cannot be blank")
        String password
) {}

