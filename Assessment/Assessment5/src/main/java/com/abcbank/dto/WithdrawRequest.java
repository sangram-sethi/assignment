package com.abcbank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/accounts/withdraw}.
 */
public record WithdrawRequest(

        @NotNull(message = "Account id is mandatory")
        Long accountId,

        @NotNull(message = "Amount is mandatory")
        @Positive(message = "Withdrawal amount must be greater than zero")
        BigDecimal amount
) {
}
