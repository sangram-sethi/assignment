package com.abcbank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/accounts/deposit}.
 */
public record DepositRequest(

        @NotNull(message = "Account id is mandatory")
        Long accountId,

        @NotNull(message = "Amount is mandatory")
        @Positive(message = "Deposit amount must be greater than zero")
        BigDecimal amount
) {
}
