package com.abcbank.dto;

import com.abcbank.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response view of a transaction ledger record.
 */
public record TransactionResponse(
        Long id,
        TransactionType transactionType,
        BigDecimal amount,
        LocalDateTime transactionDate,
        String description,
        Long accountId
) {
}
