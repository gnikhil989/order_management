package com.example.order_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet Transaction Entity (Financial Audit Ledger).
 *
 * An append-only, immutable record of every financial alteration to a wallet.
 * Captures historical before/after balances to maintain a complete mathematical audit trail.
 */
@Entity
@Table(
        name = "wallet_transactions",
        indexes = {
                @Index(name = "idx_wallet_tx_wallet_id", columnList = "walletId"),
                @Index(name = "idx_wallet_tx_created_at", columnList = "createdAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID of the wallet that this ledger entry belongs to.
     */
    @Column(nullable = false, updatable = false)
    private Long walletId;

    /**
     * Type of financial transaction (e.g. DEPOSIT, WITHDRAWAL, ORDER_PAYMENT).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private TransactionType type;

    /**
     * The monetary amount of this transaction.
     */
    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Wallet balance immediately before this transaction occurred.
     */
    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal beforeBalance;

    /**
     * Wallet balance immediately after this transaction occurred.
     */
    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal afterBalance;

    /**
     * Outcome status of the transaction (SUCCESS, FAILED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private TransactionStatus status;

    /**
     * Plain-text description or note explaining the transaction.
     */
    @Column(updatable = false, length = 255)
    private String description;

    /**
     * External reference identifier (e.g. Order ID, payment gateway reference, idempotency key).
     */
    @Column(updatable = false, length = 100)
    private String referenceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
