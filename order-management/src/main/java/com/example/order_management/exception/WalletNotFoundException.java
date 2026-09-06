package com.example.order_management.exception;

/**
 * Wallet Not Found Exception.
 *
 * Thrown when an operation targets a wallet account that does not exist in the database.
 */
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String message) {
        super(message);
    }
}
