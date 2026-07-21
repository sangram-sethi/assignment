package com.abcbank.dto;

/**
 * Response returned by the authentication endpoints, carrying the signed JWT.
 */
public record AuthResponse(String token) {
}
