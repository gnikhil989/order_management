package com.example.order_management.entity;

/**
 * Transaction Status Enum.
 *
 * Tracks the lifecycle state of each financial ledger transaction record.
 */
public enum TransactionStatus {
    /**
     * Transaction initiated and awaiting processing.
     */
    PENDING,

    /**
     * Transaction successfully processed and reflected in balance.
     */
    SUCCESS,

    /**
     * Transaction failed due to validation or processing errors.
     */
    FAILED
}
