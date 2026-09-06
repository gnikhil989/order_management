package com.example.order_management.dto.response;

import com.example.order_management.entity.Role;
import com.example.order_management.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Response Data Transfer Object (DTO).
 *
 * Represents a sanitized public view of the User entity.
 * Critical Security Rule: Never expose password hashes or sensitive internal database metadata.
 */
@Schema(description = "User profile information returned to clients")
public record UserResponse(

        @Schema(description = "Unique user ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID id,

        @Schema(description = "User's full name", example = "John Doe")
        String name,

        @Schema(description = "User's registered email", example = "john.doe@example.com")
        String email,

        @Schema(description = "User's assigned role", example = "USER")
        Role role,

        @Schema(description = "Account creation timestamp")
        LocalDateTime createdAt
) {
        /**
         * Static factory mapper method to transform a User JPA entity into a UserResponse DTO.
         *
         * @param user the managed User entity
         * @return safe UserResponse record
         */
        public static UserResponse fromEntity(User user) {
                return new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt()
                );
        }
}

