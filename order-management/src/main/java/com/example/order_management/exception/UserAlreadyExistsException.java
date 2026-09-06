package com.example.order_management.exception;

/**
 * Exception thrown when attempting to register a user with an email that is already in use.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

