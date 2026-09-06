package com.example.order_management.dto.response;

import com.example.order_management.entity.TransactionStatus;
import com.example.order_management.entity.TransactionType;
import com.example.order_management.entity.WalletTransaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet Transaction Response DTO.
 *
 * Represents an individual ledger transaction in the user's financial audit history.
 */
@Schema(description = "Financial transaction audit record")
public record WalletTransactionResponse(
        @Schema(description = "Transaction unique ID", example = "1")
        Long id,

        @Schema(description = "Wallet ID associated with this transaction", example = "1")
        Long walletId,

        @Schema(description = "Type of transaction", example = "DEPOSIT")
        TransactionType type,

        @Schema(description = "Monetary amount involved", example = "50.00")
        BigDecimal amount,

        @Schema(description = "Balance before transaction", example = "100.00")
        BigDecimal beforeBalance,

        @Schema(description = "Balance after transaction", example = "150.00")
        BigDecimal afterBalance,

        @Schema(description = "Transaction status", example = "SUCCESS")
        TransactionStatus status,

        @Schema(description = "Transaction description", example = "Wallet deposit")
        String description,

        @Schema(description = "External reference ID", example = "DEP-12345")
        String referenceId,

        @Schema(description = "Timestamp when the transaction was executed")
        LocalDateTime createdAt
) {
    /**
     * Static factory method mapping a WalletTransaction JPA entity to a WalletTransactionResponse DTO.
     *
     * @param walletTransaction the entity to map
     * @return populated WalletTransactionResponse DTO
     */
    public static WalletTransactionResponse fromEntity(WalletTransaction walletTransaction) {
        return new WalletTransactionResponse(
                walletTransaction.getId(),
                walletTransaction.getWalletId(),
                walletTransaction.getType(),
                walletTransaction.getAmount(),
                walletTransaction.getBeforeBalance(),
                walletTransaction.getAfterBalance(),
                walletTransaction.getStatus(),
                walletTransaction.getDescription(),
                walletTransaction.getReferenceId(),
                walletTransaction.getCreatedAt()
        );
    }

}


