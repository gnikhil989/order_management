package com.example.order_management.exception;

/**
 * Insufficient Balance Exception.
 *
 * Thrown when a withdrawal, transfer, or payment exceeds the available wallet balance.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
