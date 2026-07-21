package com.abcbank.service;

import com.abcbank.dto.TransactionResponse;

import java.util.List;

/**
 * Read operations over the transaction ledger. Behaviour is specified by
 * {@code TransactionServiceTest}.
 */
public interface TransactionService {

    List<TransactionResponse> getAllTransactions();

    TransactionResponse getTransactionById(Long id);

    List<TransactionResponse> getTransactionsByAccountId(Long accountId);
}
