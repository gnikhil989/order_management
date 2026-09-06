package com.example.order_management.dto.response;

import com.example.order_management.entity.Role;
import com.example.order_management.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * User Response Data Transfer Object (DTO).
 *
 * Represents a sanitized public view of the User entity.
 * Critical Security Rule: Never expose password hashes or sensitive internal database metadata.
 */
@Schema(description = "User profile information returned to clients")
public record UserResponse(

        @Schema(description = "Unique user ID", example = "1")
        Long id,

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

