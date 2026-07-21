package com.abcbank.dto;

import com.abcbank.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request body for creating or updating an account.
 */
public record AccountRequest(

        @NotBlank(message = "Account number is mandatory")
        String accountNumber,

        @NotNull(message = "Account type is mandatory")
        AccountType accountType,

        @NotNull(message = "Opening balance is mandatory")
        @PositiveOrZero(message = "Opening balance cannot be negative")
        BigDecimal balance,

        @NotNull(message = "Customer id is mandatory")
        Long customerId
) {
}
