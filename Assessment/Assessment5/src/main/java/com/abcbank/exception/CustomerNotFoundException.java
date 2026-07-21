package com.abcbank.exception;

/**
 * Thrown when a customer cannot be located. Mapped to HTTP 404.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public static CustomerNotFoundException withId(Long id) {
        return new CustomerNotFoundException("Customer not found with id: " + id);
    }
}
