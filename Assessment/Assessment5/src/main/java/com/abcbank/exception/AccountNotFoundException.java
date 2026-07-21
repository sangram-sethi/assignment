package com.abcbank.exception;

/**
 * Thrown when an account cannot be located. Mapped to HTTP 404.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public static AccountNotFoundException withId(Long id) {
        return new AccountNotFoundException("Account not found with id: " + id);
    }
}
