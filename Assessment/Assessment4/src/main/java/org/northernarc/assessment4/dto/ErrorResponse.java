package org.northernarc.assessment4.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error envelope returned by the global exception handler.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String status,
        String message,
        List<String> errors,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String status, String message) {
        return new ErrorResponse(status, message, null, LocalDateTime.now());
    }

    public static ErrorResponse of(String status, String message, List<String> errors) {
        return new ErrorResponse(status, message, errors, LocalDateTime.now());
    }
}
