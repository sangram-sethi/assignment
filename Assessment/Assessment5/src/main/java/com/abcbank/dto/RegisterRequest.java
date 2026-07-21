package com.abcbank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Payload for {@code POST /api/auth/register}.
 */
public record RegisterRequest(

        @NotBlank(message = "Name is mandatory")
        String name,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Phone is mandatory")
        @Pattern(regexp = "\\d{10}", message = "Phone number must contain exactly 10 digits")
        String phone,

        @NotBlank(message = "Password is mandatory")
        String password
) {
}
