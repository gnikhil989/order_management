package com.example.order_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Wallet Entity.
 *
 * Represents a user's digital wallet account.
 * Manages monetary balance with high-precision BigDecimal arithmetic
 * and row-level pessimistic locking for concurrency safety.
 */
@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique identifier of the user who owns this wallet.
     * Enforces a 1:1 relationship between User and Wallet.
     */
    @Column(nullable = false, unique = true)
    private Long userId;

    /**
     * Current monetary balance of the wallet.
     * Stored with precision 19 and scale 2 to prevent decimal rounding errors.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

    /**
     * Currency code (ISO 4217, e.g. "INR", "USD", "EUR").
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";


    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Credits funds to the wallet balance.
     *
     * @param amount positive monetary amount to credit
     */
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Debits funds from the wallet balance.
     *
     * @param amount positive monetary amount to debit
     */
    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_EVEN);
    }
}
