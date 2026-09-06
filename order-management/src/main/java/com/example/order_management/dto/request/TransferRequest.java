package com.example.order_management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Transfer Request DTO.
 *
 * Captures and validates incoming payload for peer-to-peer wallet transfers.
 */
@Schema(description = "Payload for transferring funds to another user's wallet")
public record TransferRequest(
        @Schema(description = "The User ID of the recipient", example = "2")
        @NotNull(message = "Recipient User ID is required")
        Long recipientUserId,

        @Schema(description = "Monetary amount to transfer (minimum 0.01)", example = "25.00")
        @NotNull(message = "Transfer amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
        BigDecimal amount,

        @Schema(description = "Optional note for the transfer", example = "Dinner split payment")
        String description
) {}

