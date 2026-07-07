package org.northernarc.assessment4.exception;

/**
 * Thrown when attempting to register a customer with an email that is already
 * in use. Surfaces as HTTP 409 Conflict.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
