package com.abcbank.service.impl;

import com.abcbank.dto.TransactionResponse;
import com.abcbank.repository.AccountRepository;
import com.abcbank.repository.TransactionRepository;
import com.abcbank.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Skeleton implementation. The production logic is intentionally absent: this
 * project is delivered test-first, and {@code TransactionServiceTest} is the
 * executable specification that this class must be built to satisfy.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final String NOT_IMPLEMENTED =
            "Not implemented yet - build against the TransactionServiceTest specification.";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
