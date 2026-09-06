package com.example.order_management.entity;

/**
 * Transaction Type Enum.
 *
 * Defines all supported financial operations in the Digital Wallet system.
 */
public enum TransactionType {
    /**
     * External funds added to the wallet balance.
     */
    DEPOSIT,

    /**
     * Funds withdrawn from the wallet to an external bank or account.
     */
    WITHDRAWAL,

    /**
     * Funds deducted from the wallet to pay for an order.
     */
    ORDER_PAYMENT,

    /**
     * Funds credited back to the wallet due to a cancelled or returned order.
     */
    REFUND,

    /**
     * Funds received from another user via peer-to-peer transfer.
     */
    TRANSFER_IN,

    /**
     * Funds sent to another user via peer-to-peer transfer.
     */
    TRANSFER_OUT
}
