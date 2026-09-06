package com.example.order_management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Withdraw Request DTO.
 *
 * Captures and validates incoming payload for withdrawing funds from a wallet.
 */
@Schema(description = "Payload for withdrawing funds from the user's wallet")
public record WithdrawRequest(
        @Schema(description = "Monetary amount to withdraw (minimum 0.01)", example = "50.00")
        @NotNull(message = "Withdrawal amount is required")
        @DecimalMin(value = "0.01", message = "Withdrawal amount must be at least 0.01")
        BigDecimal amount,

        @Schema(description = "Optional note or reference for the withdrawal", example = "Bank transfer withdrawal")
        String description
) {}
