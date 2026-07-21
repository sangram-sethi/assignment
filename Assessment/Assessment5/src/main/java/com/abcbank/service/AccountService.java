package com.abcbank.service;

import com.abcbank.dto.AccountRequest;
import com.abcbank.dto.AccountResponse;
import com.abcbank.dto.DepositRequest;
import com.abcbank.dto.TransactionResponse;
import com.abcbank.dto.TransferRequest;
import com.abcbank.dto.WithdrawRequest;

import java.util.List;

/**
 * Account and banking-operation domain logic. Behaviour is specified by
 * {@code AccountServiceTest}.
 */
public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    List<AccountResponse> getAllAccounts();

    AccountResponse getAccountById(Long id);

    AccountResponse updateAccount(Long id, AccountRequest request);

    void deleteAccount(Long id);

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse withdraw(WithdrawRequest request);

    /**
     * Atomically move money between two accounts.
     *
     * @return the two transaction records created (debit on the source, credit on the destination)
     */
    List<TransactionResponse> transfer(TransferRequest request);
}
