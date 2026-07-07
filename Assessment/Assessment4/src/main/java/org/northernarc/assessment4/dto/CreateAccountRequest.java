package org.northernarc.assessment4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Inbound payload for opening an account.
 */
public record CreateAccountRequest(
        @NotBlank(message = "Account number is required")
        String accountNumber,

        @NotBlank(message = "Account type is required")
        String accountType,

        @NotNull(message = "Balance is required")
        @PositiveOrZero(message = "Balance must be zero or a positive amount")
        Double balance
) {
}
