package com.example.order_management.dto.response;

import com.example.order_management.entity.Wallet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet Response DTO.
 *
 * Sanitized response payload returning wallet balance and metadata.
 */
@Schema(description = "Wallet account details and current balance")
public record WalletResponse(
        @Schema(description = "Wallet unique ID", example = "1")
        Long id,

        @Schema(description = "User ID of the wallet owner", example = "1")
        Long userId,

        @Schema(description = "Current available balance", example = "150.00")
        BigDecimal balance,

        @Schema(description = "Account currency", example = "INR")
        String currency,


        @Schema(description = "Timestamp when the wallet was last updated")
        LocalDateTime updatedAt
) {
        /**
         * Static factory method mapping a Wallet JPA entity to a WalletResponse DTO.
         *
         * @param wallet the entity to map
         * @return populated WalletResponse DTO
         */
        public static WalletResponse fromEntity(Wallet wallet) {
                return new WalletResponse(
                                wallet.getId(),
                                wallet.getUserId(),
                                wallet.getBalance(),
                                wallet.getCurrency(),
                                wallet.getUpdatedAt()
                );
        }
}

