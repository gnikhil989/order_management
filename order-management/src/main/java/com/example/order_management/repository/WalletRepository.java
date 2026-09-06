package com.example.order_management.repository;

import com.example.order_management.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Wallet Repository Interface.
 *
 * Provides database operations for Wallet entities, including row-level
 * Pessimistic Write Locking (SELECT ... FOR UPDATE) to prevent race conditions.
 */
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Finds a wallet by the owner's User ID without locking (Read-only queries).
     *
     * @param userId owner's unique user identifier
     * @return Optional containing the Wallet if found, empty Optional otherwise
     */
    Optional<Wallet> findByUserId(Long userId);

    /**
     * Finds a wallet by User ID and acquires an exclusive PESSIMISTIC_WRITE lock on the row.
     * Prevents other concurrent transactions from reading (with lock) or modifying the row.
     *
     * @param userId owner's unique user identifier
     * @return Optional containing the locked Wallet if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wallet FROM Wallet wallet WHERE wallet.userId = :userId")
    Optional<Wallet> findByUserIdWithLock(@Param("userId") Long userId);

    /**
     * Finds a wallet by primary key ID and acquires an exclusive PESSIMISTIC_WRITE lock on the row.
     *
     * @param walletId wallet unique identifier
     * @return Optional containing the locked Wallet if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wallet FROM Wallet wallet WHERE wallet.id = :walletId")
    Optional<Wallet> findByIdWithLock(@Param("walletId") Long walletId);
}


