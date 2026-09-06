package com.example.order_management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authentication Response Data Transfer Object (DTO).
 *
 * Returned after successful user registration or login.
 * Contains the signed JWT access token and user profile details.
 */
@Schema(description = "Response returned upon successful login or registration containing JWT token")
public record AuthResponse(

        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Authentication scheme type", example = "Bearer")
        String tokenType,

        @Schema(description = "Token expiration duration in milliseconds", example = "86400000")
        long expiresIn,

        @Schema(description = "Authenticated user profile")
        UserResponse user
) {
        /**
         * Convenience constructor defaulting tokenType to 'Bearer'.
         */
        public AuthResponse(String accessToken, long expiresIn, UserResponse user) {
                this(accessToken, "Bearer", expiresIn, user);
        }
}

