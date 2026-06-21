package com.assignment.socialmedia.exception;

/**
 * Thrown when creating an entity would violate a uniqueness constraint (for
 * example a duplicate username, email or role name). Mapped to HTTP 409 by
 * {@link GlobalExceptionHandler}.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
