package com.abcbank.exception;

/**
 * Thrown when a banking operation violates a business rule (for example a
 * non-positive amount, or a transfer whose source and destination are the same
 * account). Mapped to HTTP 400 Bad Request.
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
