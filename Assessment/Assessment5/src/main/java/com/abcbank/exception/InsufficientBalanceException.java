package com.abcbank.exception;

/**
 * Thrown when a withdrawal or transfer would exceed the available balance.
 * Mapped to HTTP 400 Bad Request.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
