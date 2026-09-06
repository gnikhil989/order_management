package com.example.order_management.repository;

import com.example.order_management.entity.TransactionType;
import com.example.order_management.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Wallet Transaction Repository Interface.
 *
 * Provides database operations for querying the immutable financial audit ledger.
 */
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    /**
     * Retrieves all ledger transactions for a given wallet, ordered chronologically newest first.
     *
     * @param walletId wallet identifier
     * @return list of WalletTransaction records
     */
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    /**
     * Retrieves a paginated slice of ledger transactions for a wallet (for Passbook view).
     *
     * @param walletId wallet identifier
     * @param pageable pagination and sorting parameters
     * @return Page of WalletTransaction records
     */
    Page<WalletTransaction> findByWalletId(Long walletId, Pageable pageable);

    /**
     * Retrieves a paginated slice of ledger transactions filtered by transaction type.
     *
     * @param walletId wallet identifier
     * @param type transaction type filter (e.g. DEPOSIT, WITHDRAWAL, TRANSFER_OUT)
     * @param pageable pagination and sorting parameters
     * @return Page of filtered WalletTransaction records
     */
    Page<WalletTransaction> findByWalletIdAndType(Long walletId, TransactionType type, Pageable pageable);
}

