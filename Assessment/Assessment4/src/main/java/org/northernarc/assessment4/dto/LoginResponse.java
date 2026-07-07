package org.northernarc.assessment4.dto;

/**
 * Response returned after a successful authentication, carrying the signed JWT.
 */
public record LoginResponse(String token) {
}
