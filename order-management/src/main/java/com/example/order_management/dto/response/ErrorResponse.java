package com.example.order_management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standard Error Response DTO.
 *
 * Returned consistently by GlobalExceptionHandler for all API error responses.
 */
@Schema(description = "Standardized error response payload")
public record ErrorResponse(

        @Schema(description = "Error category code", example = "USER_ALREADY_EXISTS")
        String errorCode,

        @Schema(description = "Human-readable error description", example = "An account with this email already exists.")
        String message,

        @Schema(description = "HTTP status code integer", example = "409")
        int status,

        @Schema(description = "Timestamp when the error occurred")
        LocalDateTime timestamp
) {
        public ErrorResponse(String errorCode, String message, int status) {
                this(errorCode, message, status, LocalDateTime.now());
        }
}

