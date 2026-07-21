package com.abcbank.testsupport;

import com.abcbank.dto.AccountRequest;
import com.abcbank.dto.CustomerRequest;
import com.abcbank.dto.DepositRequest;
import com.abcbank.dto.RegisterRequest;
import com.abcbank.dto.TransferRequest;
import com.abcbank.dto.WithdrawRequest;
import com.abcbank.entity.Account;
import com.abcbank.entity.AccountType;
import com.abcbank.entity.Customer;
import com.abcbank.entity.Transaction;
import com.abcbank.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Central factory of test data. Keeping construction in one place removes
 * duplication across the test suite and gives every test consistent, valid
 * defaults that individual tests can override where relevant.
 */
public final class TestFixtures {

    public static final String VALID_EMAIL = "alice@abcbank.com";
    public static final String VALID_PHONE = "1234567890";
    public static final String RAW_PASSWORD = "Secret123!";
    public static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHashValueForTesting123456";

    private TestFixtures() {
    }

    // ----- Customers -------------------------------------------------------

    public static Customer customer(Long id, String name, String email) {
        return new Customer(id, name, email, VALID_PHONE, ENCODED_PASSWORD);
    }

    public static Customer alice() {
        return customer(1L, "Alice", VALID_EMAIL);
    }

    public static CustomerRequest customerRequest() {
        return new CustomerRequest("Alice", VALID_EMAIL, VALID_PHONE, RAW_PASSWORD);
    }

    public static CustomerRequest customerRequest(String name, String email, String phone, String password) {
        return new CustomerRequest(name, email, phone, password);
    }

    public static RegisterRequest registerRequest() {
        return new RegisterRequest("Alice", VALID_EMAIL, VALID_PHONE, RAW_PASSWORD);
    }

    // ----- Accounts --------------------------------------------------------

    public static Account account(Long id, String number, AccountType type, BigDecimal balance, Customer owner) {
        return new Account(id, number, type, balance, owner);
    }

    public static Account savingsAccount(Long id, BigDecimal balance, Customer owner) {
        return account(id, "ACC-" + id, AccountType.SAVINGS, balance, owner);
    }

    public static AccountRequest accountRequest(Long customerId) {
        return new AccountRequest("ACC-1001", AccountType.SAVINGS, new BigDecimal("500.00"), customerId);
    }

    public static AccountRequest accountRequest(String number, AccountType type, BigDecimal balance, Long customerId) {
        return new AccountRequest(number, type, balance, customerId);
    }

    // ----- Banking operation requests -------------------------------------

    public static DepositRequest depositRequest(Long accountId, String amount) {
        return new DepositRequest(accountId, new BigDecimal(amount));
    }

    public static WithdrawRequest withdrawRequest(Long accountId, String amount) {
        return new WithdrawRequest(accountId, new BigDecimal(amount));
    }

    public static TransferRequest transferRequest(Long from, Long to, String amount) {
        return new TransferRequest(from, to, new BigDecimal(amount));
    }

    // ----- Transactions ----------------------------------------------------

    public static Transaction transaction(Long id, TransactionType type, String amount, Account account) {
        return new Transaction(id, type, new BigDecimal(amount), LocalDateTime.now(), type.name(), account);
    }
}
