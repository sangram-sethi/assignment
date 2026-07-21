package com.abcbank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/accounts/transfer}.
 */
public record TransferRequest(

        @NotNull(message = "Source account id is mandatory")
        Long sourceAccountId,

        @NotNull(message = "Destination account id is mandatory")
        Long destinationAccountId,

        @NotNull(message = "Amount is mandatory")
        @Positive(message = "Transfer amount must be positive")
        BigDecimal amount
) {
}
