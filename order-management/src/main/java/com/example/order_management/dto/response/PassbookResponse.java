package com.example.order_management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Passbook / Financial Statement Response DTO.
 *
 * Provides a comprehensive bank-style passbook statement including:
 * - Current wallet balance
 * - Aggregated credit & debit totals
 * - Paginated transaction history ledger entries
 * - Page navigation metadata (currentPage, totalPages, totalTransactions)
 */
@Schema(description = "Bank-style digital passbook statement with summarized totals and paginated transactions")
public record PassbookResponse(
        @Schema(description = "Wallet ID", example = "1")
        Long walletId,

        @Schema(description = "Current available balance", example = "250.00")
        BigDecimal currentBalance,

        @Schema(description = "Account currency", example = "INR")
        String currency,


        @Schema(description = "Total lifetime deposits credited to this wallet", example = "500.00")
        BigDecimal totalDeposits,

        @Schema(description = "Total lifetime withdrawals debited from this wallet", example = "150.00")
        BigDecimal totalWithdrawals,

        @Schema(description = "Total lifetime P2P transfers sent out", example = "100.00")
        BigDecimal totalTransfersSent,

        @Schema(description = "Total lifetime P2P transfers received in", example = "0.00")
        BigDecimal totalTransfersReceived,

        @Schema(description = "Total number of transactions matching filter criteria", example = "25")
        long totalTransactions,

        @Schema(description = "Current page index (0-based)", example = "0")
        int currentPage,

        @Schema(description = "Total number of pages", example = "3")
        int totalPages,

        @Schema(description = "Number of items per page", example = "10")
        int pageSize,

        @Schema(description = "List of ledger transactions for the current page")
        List<WalletTransactionResponse> transactions
) {}

