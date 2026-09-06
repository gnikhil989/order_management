package com.example.order_management.service;

import com.example.order_management.dto.request.DepositRequest;
import com.example.order_management.dto.request.TransferRequest;
import com.example.order_management.dto.request.WithdrawRequest;
import com.example.order_management.dto.response.PassbookResponse;
import com.example.order_management.dto.response.WalletResponse;
import com.example.order_management.dto.response.WalletTransactionResponse;
import com.example.order_management.entity.TransactionStatus;
import com.example.order_management.entity.TransactionType;
import com.example.order_management.entity.Wallet;
import com.example.order_management.entity.WalletTransaction;
import com.example.order_management.exception.InsufficientBalanceException;
import com.example.order_management.exception.WalletNotFoundException;
import com.example.order_management.repository.WalletRepository;
import com.example.order_management.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Wallet Service.
 *
 * Implements core financial business logic for the Digital Wallet subsystem:
 * - High-precision balance operations (BigDecimal with Banker's Rounding).
 * - Pessimistic write locking on database rows to prevent double-spending race conditions.
 * - Deterministic lock ordering during multi-wallet transfers to prevent deadlocks.
 * - Append-only immutable financial audit ledger creation.
 * - Follows DRY (Don't Repeat Yourself) principle with extracted helper methods.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    /**
     * Retrieves the wallet for the given user, creating a default zero-balance wallet if none exists.
     *
     * @param userId user identifier
     * @return WalletResponse DTO
     */
    @Transactional
    public WalletResponse getOrCreateWallet(Long userId) {
        log.debug("Fetching or creating wallet for user: {}", userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultWallet(userId));
        return WalletResponse.fromEntity(wallet);
    }

    /**
     * Deposits funds into the user's wallet.
     *
     * 1. Acquires a pessimistic write lock on the user's wallet row.
     * 2. Credits the amount to the balance.
     * 3. Records an immutable audit ledger entry (DEPOSIT).
     *
     * @param userId user identifier
     * @param request validated deposit payload
     * @return updated WalletResponse
     */
    @Transactional
    public WalletResponse deposit(Long userId, DepositRequest request) {
        log.info("Processing deposit of {} for user: {}", request.amount(), userId);

        // Step 1: Lock wallet row (or create if first-time user)
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> createDefaultWallet(userId));

        BigDecimal beforeBalance = wallet.getBalance();
        BigDecimal depositAmount = scaleAmount(request.amount());

        // Step 2: Update balance
        wallet.credit(depositAmount);
        Wallet updatedWallet = walletRepository.save(wallet);

        // Step 3: Record immutable audit ledger entry (DRY helper)
        String description = request.description() != null ? request.description() : "Wallet Deposit";
        createAndSaveTransaction(
                updatedWallet.getId(),
                TransactionType.DEPOSIT,
                depositAmount,
                beforeBalance,
                updatedWallet.getBalance(),
                description,
                generateReferenceId("DEP")
        );

        log.info("Deposit successful for user: {}. New balance: {}", userId, updatedWallet.getBalance());
        return WalletResponse.fromEntity(updatedWallet);
    }

    /**
     * Withdraws funds from the user's wallet.
     *
     * 1. Acquires a pessimistic write lock on the user's wallet row.
     * 2. Validates that the current balance is sufficient.
     * 3. Debits the amount from the balance.
     * 4. Records an immutable audit ledger entry (WITHDRAWAL).
     *
     * @param userId user identifier
     * @param request validated withdrawal payload
     * @return updated WalletResponse
     * @throws InsufficientBalanceException if current balance < withdrawal amount
     */
    @Transactional
    public WalletResponse withdraw(Long userId, WithdrawRequest request) {
        log.info("Processing withdrawal of {} for user: {}", request.amount(), userId);

        // Step 1: Lock wallet row
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        BigDecimal beforeBalance = wallet.getBalance();
        BigDecimal withdrawAmount = scaleAmount(request.amount());

        // Step 2: Validate balance sufficiency
        if (beforeBalance.compareTo(withdrawAmount) < 0) {
            log.warn("Withdrawal failed for user: {}. Balance {} is less than requested amount {}",
                    userId, beforeBalance, withdrawAmount);
            throw new InsufficientBalanceException(
                    "Insufficient balance. Current balance: " + beforeBalance + ", Requested: " + withdrawAmount
            );
        }

        // Step 3: Update balance
        wallet.debit(withdrawAmount);
        Wallet updatedWallet = walletRepository.save(wallet);

        // Step 4: Record immutable audit ledger entry (DRY helper)
        String description = request.description() != null ? request.description() : "Wallet Withdrawal";
        createAndSaveTransaction(
                updatedWallet.getId(),
                TransactionType.WITHDRAWAL,
                withdrawAmount,
                beforeBalance,
                updatedWallet.getBalance(),
                description,
                generateReferenceId("WTH")
        );

        log.info("Withdrawal successful for user: {}. New balance: {}", userId, updatedWallet.getBalance());
        return WalletResponse.fromEntity(updatedWallet);
    }

    /**
     * Transfers funds from one user's wallet to another user's wallet.
     *
     * Uses Deterministic Lock Ordering (sorting user IDs) to eliminate circular deadlocks.
     * Executes both credit and debit operations atomically within a single @Transactional block.
     *
     * @param senderUserId user sending funds
     * @param request transfer payload containing recipient user ID and amount
     * @return updated sender WalletResponse
     */
    @Transactional
    public WalletResponse transfer(Long senderUserId, TransferRequest request) {
        Long recipientUserId = request.recipientUserId();
        log.info("Processing P2P transfer of {} from user {} to user {}",
                request.amount(), senderUserId, recipientUserId);

        // Step 1: Validate not self-transfer
        if (senderUserId.equals(recipientUserId)) {
            throw new IllegalArgumentException("Cannot transfer funds to your own wallet.");
        }

        BigDecimal transferAmount = scaleAmount(request.amount());

        // Step 2: Deadlock Prevention - Acquire locks in deterministic sorted order
        Long lowerNumberedUserId = senderUserId < recipientUserId ? senderUserId : recipientUserId;
        Long higherNumberedUserId = senderUserId < recipientUserId ? recipientUserId : senderUserId;

        Wallet firstLockedWallet = walletRepository.findByUserIdWithLock(lowerNumberedUserId)
                .orElseGet(() -> createDefaultWallet(lowerNumberedUserId));
        Wallet secondLockedWallet = walletRepository.findByUserIdWithLock(higherNumberedUserId)
                .orElseGet(() -> createDefaultWallet(higherNumberedUserId));

        Wallet senderWallet = senderUserId.equals(lowerNumberedUserId) ? firstLockedWallet : secondLockedWallet;
        Wallet recipientWallet = recipientUserId.equals(lowerNumberedUserId) ? firstLockedWallet : secondLockedWallet;

        // Step 3: Validate sender balance
        BigDecimal senderBeforeBalance = senderWallet.getBalance();
        if (senderBeforeBalance.compareTo(transferAmount) < 0) {
            log.warn("Transfer failed: sender {} has insufficient balance {}", senderUserId, senderBeforeBalance);
            throw new InsufficientBalanceException(
                    "Insufficient balance for transfer. Current balance: " + senderBeforeBalance + ", Required: " + transferAmount
            );
        }

        BigDecimal recipientBeforeBalance = recipientWallet.getBalance();

        // Step 4: Perform atomic balance updates
        senderWallet.debit(transferAmount);
        recipientWallet.credit(transferAmount);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        String transferReference = generateReferenceId("TRF");

        // Step 5: Record double-entry audit ledger (TRANSFER_OUT for sender, TRANSFER_IN for recipient)
        String optionalTransferNote = (request.description() != null ? " - " + request.description() : "");
        createAndSaveTransaction(
                senderWallet.getId(),
                TransactionType.TRANSFER_OUT,
                transferAmount,
                senderBeforeBalance,
                senderWallet.getBalance(),
                "Transfer to user: " + recipientUserId + optionalTransferNote,
                transferReference
        );

        createAndSaveTransaction(
                recipientWallet.getId(),
                TransactionType.TRANSFER_IN,
                transferAmount,
                recipientBeforeBalance,
                recipientWallet.getBalance(),
                "Transfer from user: " + senderUserId + optionalTransferNote,
                transferReference
        );

        log.info("P2P transfer completed successfully. Ref: {}", transferReference);
        return WalletResponse.fromEntity(senderWallet);
    }

    /**
     * Retrieves the chronological audit transaction history for a user's wallet.
     *
     * @param userId user identifier
     * @return list of WalletTransactionResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactionHistory(Long userId) {
        log.debug("Fetching transaction history for user: {}", userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(WalletTransactionResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves a paginated digital passbook statement with summarized lifetime credit/debit totals.
     *
     * @param userId user identifier
     * @param page zero-based page index
     * @param size page size (e.g. 10 items)
     * @param transactionTypeFilter optional filter by transaction type (e.g. DEPOSIT, WITHDRAWAL)
     * @return PassbookResponse containing totals and paginated transactions
     */
    @Transactional(readOnly = true)
    public PassbookResponse getPassbook(Long userId, int page, int size, TransactionType transactionTypeFilter) {
        log.debug("Fetching passbook for user: {}, page: {}, size: {}, type: {}", userId, page, size, transactionTypeFilter);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultWallet(userId));

        // 1. Fetch paginated ledger records
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WalletTransaction> transactionPage = (transactionTypeFilter != null)
                ? transactionRepository.findByWalletIdAndType(wallet.getId(), transactionTypeFilter, pageable)
                : transactionRepository.findByWalletId(wallet.getId(), pageable);

        // 2. Fetch all historical transactions for this wallet to compute passbook summary metrics
        List<WalletTransaction> allHistoricalTransactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());

        // DRY Metric Calculation via extracted sumTransactions helper
        BigDecimal totalDeposits = sumTransactions(allHistoricalTransactions, TransactionType.DEPOSIT, TransactionType.REFUND);
        BigDecimal totalWithdrawals = sumTransactions(allHistoricalTransactions, TransactionType.WITHDRAWAL, TransactionType.ORDER_PAYMENT);
        BigDecimal totalTransfersSent = sumTransactions(allHistoricalTransactions, TransactionType.TRANSFER_OUT);
        BigDecimal totalTransfersReceived = sumTransactions(allHistoricalTransactions, TransactionType.TRANSFER_IN);

        List<WalletTransactionResponse> transactionResponses = transactionPage.getContent()
                .stream()
                .map(WalletTransactionResponse::fromEntity)
                .toList();

        return new PassbookResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                totalDeposits,
                totalWithdrawals,
                totalTransfersSent,
                totalTransfersReceived,
                transactionPage.getTotalElements(),
                transactionPage.getNumber(),
                transactionPage.getTotalPages(),
                transactionPage.getSize(),
                transactionResponses
        );
    }

    /**
     * Helper method to initialize a new default wallet with 0.00 balance.
     */
    private Wallet createDefaultWallet(Long userId) {
        log.info("Creating initial zero-balance wallet for user: {}", userId);
        Wallet newWallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                .currency("INR")
                .build();
        return walletRepository.save(newWallet);
    }

    /**
     * DRY Helper: Scales a monetary amount to 2 decimal places using Banker's Rounding (HALF_EVEN).
     */
    private BigDecimal scaleAmount(BigDecimal monetaryAmount) {
        return monetaryAmount.setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * DRY Helper: Generates a human-readable unique reference identifier (e.g. DEP-A1B2C3D4).
     */
    private String generateReferenceId(String transactionTypePrefix) {
        return transactionTypePrefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * DRY Helper: Creates and persists an immutable WalletTransaction ledger entry.
     */
    private WalletTransaction createAndSaveTransaction(
            Long walletId,
            TransactionType transactionType,
            BigDecimal monetaryAmount,
            BigDecimal balanceBeforeTransaction,
            BigDecimal balanceAfterTransaction,
            String transactionDescription,
            String referenceIdentifier
    ) {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(walletId)
                .type(transactionType)
                .amount(monetaryAmount)
                .beforeBalance(balanceBeforeTransaction)
                .afterBalance(balanceAfterTransaction)
                .status(TransactionStatus.SUCCESS)
                .description(transactionDescription)
                .referenceId(referenceIdentifier)
                .build();
        return transactionRepository.save(transaction);
    }

    /**
     * DRY Helper: Aggregates monetary sums for specific transaction types across the audit ledger.
     */
    private BigDecimal sumTransactions(List<WalletTransaction> historicalTransactions, TransactionType... targetTransactionTypes) {
        Set<TransactionType> targetTypeSet = Set.of(targetTransactionTypes);
        return historicalTransactions.stream()
                .filter(transaction -> targetTypeSet.contains(transaction.getType()))
                .map(WalletTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}



