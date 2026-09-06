package com.example.order_management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Deposit Request DTO.
 *
 * Captures and validates incoming payload for topping up a wallet balance.
 */
@Schema(description = "Payload for depositing funds into the user's wallet")
public record DepositRequest(
        @Schema(description = "Monetary amount to deposit (minimum 0.01)", example = "100.00")
        @NotNull(message = "Deposit amount is required")
        @DecimalMin(value = "0.01", message = "Deposit amount must be at least 0.01")
        BigDecimal amount,

        @Schema(description = "Optional note or reference for the deposit", example = "Initial wallet top-up")
        String description
) {}
