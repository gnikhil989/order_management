package com.example.order_management.controller;

import com.example.order_management.dto.request.DepositRequest;
import com.example.order_management.dto.request.TransferRequest;
import com.example.order_management.dto.request.WithdrawRequest;
import com.example.order_management.dto.response.PassbookResponse;
import com.example.order_management.dto.response.WalletResponse;
import com.example.order_management.dto.response.WalletTransactionResponse;
import com.example.order_management.entity.User;
import com.example.order_management.repository.UserRepository;
import com.example.order_management.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Wallet Controller.
 *
 * Thin REST Controller exposing endpoints for digital wallet operations:
 * - Viewing current balance
 * - Depositing funds
 * - Withdrawing funds
 * - Peer-to-peer transfers
 * - Retrieving chronological audit transaction history
 *
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Endpoints for digital wallet balance, deposits, withdrawals, transfers, and ledger history")
@SecurityRequirement(name = "BearerAuth")
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    /**
     * Retrieves the current user's wallet details and balance.
     */
    @GetMapping
    @Operation(summary = "Get current user wallet", description = "Returns the current wallet details and balance for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    public ResponseEntity<WalletResponse> getWallet(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(walletService.getOrCreateWallet(userId));
    }

    /**
     * Deposits money into the current user's wallet.
     */
    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into wallet", description = "Credits the specified amount to the user's wallet and records an audit ledger entry.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit processed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error - Invalid amount"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<WalletResponse> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequest request
    ) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(walletService.deposit(userId, request));
    }

    /**
     * Withdraws money from the current user's wallet.
     */
    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from wallet", description = "Debits the specified amount from the user's wallet after verifying sufficient balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal processed successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid amount"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<WalletResponse> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request
    ) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(walletService.withdraw(userId, request));
    }

    /**
     * Transfers money from the current user's wallet to another user's wallet.
     */
    @PostMapping("/transfer")
    @Operation(summary = "Peer-to-peer wallet transfer", description = "Atomically transfers funds from the current user's wallet to another user's wallet with deadlock prevention.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient balance or self-transfer attempt"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<WalletResponse> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request
    ) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(walletService.transfer(userId, request));
    }

    /**
     * Retrieves the chronological audit transaction history for the user's wallet.
     */
    @GetMapping("/transactions")
    @Operation(summary = "Get wallet transaction history", description = "Returns an immutable audit list of all credits, debits, and transfers for this wallet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<WalletTransactionResponse>> getTransactionHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }

    /**
     * Retrieves a digital passbook statement with summarized lifetime credit/debit totals and paginated transactions.
     */
    @GetMapping("/passbook")
    @Operation(
            summary = "Get digital passbook statement",
            description = "Returns a comprehensive passbook statement including current balance, lifetime deposit/withdrawal/transfer totals, and paginated transaction ledger entries."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passbook retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PassbookResponse> getPassbook(
            @AuthenticationPrincipal UserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.example.order_management.entity.TransactionType type
    ) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(walletService.getPassbook(userId, page, size, type));
    }

    /**
     * Helper to resolve the authenticated User's ID from their UserDetails email.
     */
    private Long resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
        return user.getId();
    }
}

