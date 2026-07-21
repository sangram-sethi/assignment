package com.abcbank.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error envelope returned by the {@code GlobalExceptionHandler}.
 *
 * @param status     the numeric HTTP status code
 * @param error      the HTTP reason phrase
 * @param message    a human-readable description of the problem
 * @param timestamp  when the error was produced
 * @param fieldErrors optional per-field validation messages (may be {@code null})
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now(), null);
    }
}
