package org.northernarc.assessment4.exception;

/**
 * Thrown when a requested customer cannot be located.
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
