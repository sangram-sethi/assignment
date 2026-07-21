package com.abcbank.dto;

import com.abcbank.entity.AccountType;

import java.math.BigDecimal;

/**
 * Response view of an account.
 */
public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        Long customerId
) {
}
