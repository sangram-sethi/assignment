package com.abcbank.exception;

/**
 * Thrown when attempting to register a customer with an email that already
 * exists. Mapped to HTTP 409 Conflict.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public static DuplicateEmailException forEmail(String email) {
        return new DuplicateEmailException("Email already registered: " + email);
    }
}
