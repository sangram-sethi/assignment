package com.abcbank.service.impl;

import com.abcbank.dto.AccountRequest;
import com.abcbank.dto.AccountResponse;
import com.abcbank.dto.DepositRequest;
import com.abcbank.dto.TransactionResponse;
import com.abcbank.dto.TransferRequest;
import com.abcbank.dto.WithdrawRequest;
import com.abcbank.repository.AccountRepository;
import com.abcbank.repository.CustomerRepository;
import com.abcbank.repository.TransactionRepository;
import com.abcbank.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Skeleton implementation. The production logic is intentionally absent: this
 * project is delivered test-first, and {@code AccountServiceTest} is the
 * executable specification that this class must be built to satisfy.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private static final String NOT_IMPLEMENTED =
            "Not implemented yet - build against the AccountServiceTest specification.";

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepository accountRepository,
                              CustomerRepository customerRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AccountResponse createAccount(AccountRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void deleteAccount(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public TransactionResponse deposit(DepositRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<TransactionResponse> transfer(TransferRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
