package com.example.order_management.dto.request;

import com.example.order_management.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Register Request Data Transfer Object (DTO).
 *
 * Captures and validates user registration information from the client.
 * Using Java Records guarantees immutability and concise syntax.
 */
@Schema(description = "Payload for registering a new user account")
public record RegisterRequest(

        @Schema(description = "Full name of the user", example = "John Doe")
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @Schema(description = "Unique email address used for login", example = "john.doe@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Please provide a valid email format")
        String email,

        @Schema(description = "Account password (minimum 6 characters)", example = "Secret123!")
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters long")
        String password,

        @Schema(description = "Optional role for the account. Defaults to USER if not specified.", example = "USER")
        Role role
) {
        /**
         * Compact constructor to default role to USER if client omitted it.
         */
        public RegisterRequest {
                if (role == null) {
                        role = Role.USER;
                }
        }
}

