package com.music.track.exception;

import java.time.Instant;

/**
 * Standard error payload returned by the API for all handled exceptions.
 *
 * @param timestamp when the error occurred
 * @param status    the HTTP status code
 * @param error     the HTTP status reason phrase
 * @param message   a human-readable description of the problem
 * @param path      the request path that produced the error
 */
public record ErrorResponse(Instant timestamp,
                            int status,
                            String error,
                            String message,
                            String path) {
}
