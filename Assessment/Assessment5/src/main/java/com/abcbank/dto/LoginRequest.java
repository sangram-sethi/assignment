package com.abcbank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload for {@code POST /api/auth/login}.
 */
public record LoginRequest(

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is mandatory")
        String password
) {
}
