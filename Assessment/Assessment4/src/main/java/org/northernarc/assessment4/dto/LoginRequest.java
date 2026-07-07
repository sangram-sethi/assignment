package org.northernarc.assessment4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credentials submitted to the authentication endpoint.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
