package com.abcbank.service;

import com.abcbank.dto.AccountRequest;
import com.abcbank.dto.AccountResponse;
import com.abcbank.dto.TransactionResponse;
import com.abcbank.entity.Account;
import com.abcbank.entity.AccountType;
import com.abcbank.entity.Customer;
import com.abcbank.entity.Transaction;
import com.abcbank.entity.TransactionType;
import com.abcbank.exception.AccountNotFoundException;
import com.abcbank.exception.CustomerNotFoundException;
import com.abcbank.exception.InsufficientBalanceException;
import com.abcbank.exception.InvalidTransactionException;
import com.abcbank.repository.AccountRepository;
import com.abcbank.repository.CustomerRepository;
import com.abcbank.repository.TransactionRepository;
import com.abcbank.service.impl.AccountServiceImpl;
import com.abcbank.testsupport.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Executable specification for the account service and banking operations
 * (deposit, withdraw, transfer), including all associated business rules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService specification")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Nested
    @DisplayName("createAccount")
    class CreateAccount {

        @Test
        @DisplayName("creates an account when the customer exists and the number is unique")
        void createAccount_withValidRequest_returnsCreatedAccount() {
            // Arrange
            Customer owner = TestFixtures.alice();
            AccountRequest request = TestFixtures.accountRequest(1L);
            given(customerRepository.findById(1L)).willReturn(Optional.of(owner));
            given(accountRepository.existsByAccountNumber(request.accountNumber())).willReturn(false);
            given(accountRepository.save(any(Account.class)))
                    .willReturn(TestFixtures.account(10L, request.accountNumber(),
                            request.accountType(), request.balance(), owner));

            // Act
            AccountResponse response = accountService.createAccount(request);

            // Assert
            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.accountNumber()).isEqualTo("ACC-1001");
            assertThat(response.accountType()).isEqualTo(AccountType.SAVINGS);
            assertThat(response.balance()).isEqualByComparingTo("500.00");
            assertThat(response.customerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("throws CustomerNotFoundException when the owning customer does not exist")
        void createAccount_whenCustomerMissing_throwsCustomerNotFoundException() {
            // Arrange
            AccountRequest request = TestFixtures.accountRequest(99L);
            given(customerRepository.findById(99L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.createAccount(request))
                    .isInstanceOf(CustomerNotFoundException.class);
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("rejects a duplicate account number")
        void createAccount_withDuplicateAccountNumber_throwsInvalidTransactionException() {
            // Arrange
            Customer owner = TestFixtures.alice();
            AccountRequest request = TestFixtures.accountRequest(1L);
            given(customerRepository.findById(1L)).willReturn(Optional.of(owner));
            given(accountRepository.existsByAccountNumber(request.accountNumber())).willReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> accountService.createAccount(request))
                    .isInstanceOf(InvalidTransactionException.class);
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("rejects a negative opening balance")
        void createAccount_withNegativeOpeningBalance_throwsInvalidTransactionException() {
            // Arrange
            Customer owner = TestFixtures.alice();
            AccountRequest request = TestFixtures.accountRequest(
                    "ACC-2002", AccountType.CURRENT, new BigDecimal("-1.00"), 1L);
            given(customerRepository.findById(1L)).willReturn(Optional.of(owner));

            // Act & Assert
            assertThatThrownBy(() -> accountService.createAccount(request))
                    .isInstanceOf(InvalidTransactionException.class);
            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("read operations")
    class ReadOperations {

        @Test
        @DisplayName("returns all accounts")
        void getAllAccounts_returnsMappedAccounts() {
            // Arrange
            Customer owner = TestFixtures.alice();
            given(accountRepository.findAll()).willReturn(List.of(
                    TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), owner),
                    TestFixtures.savingsAccount(2L, new BigDecimal("200.00"), owner)));

            // Act
            List<AccountResponse> responses = accountService.getAllAccounts();

            // Assert
            assertThat(responses).hasSize(2)
                    .extracting(AccountResponse::id)
                    .containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("returns an account by id when it exists")
        void getAccountById_whenExists_returnsAccount() {
            // Arrange
            Account account = TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));

            // Act
            AccountResponse response = accountService.getAccountById(1L);

            // Assert
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.balance()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("throws AccountNotFoundException when the account id is unknown")
        void getAccountById_whenMissing_throwsAccountNotFoundException() {
            // Arrange
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.getAccountById(99L))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("throws AccountNotFoundException when deleting an unknown account")
        void deleteAccount_whenMissing_throwsAccountNotFoundException() {
            // Arrange
            given(accountRepository.existsById(99L)).willReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> accountService.deleteAccount(99L))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("deletes the account when the id exists")
        void deleteAccount_whenExists_deletesAccount() {
            // Arrange
            given(accountRepository.existsById(1L)).willReturn(true);

            // Act
            accountService.deleteAccount(1L);

            // Assert
            verify(accountRepository).deleteById(1L);
        }

        @Test
        @DisplayName("updates and returns the account when the id exists")
        void updateAccount_whenExists_updatesFields() {
            // Arrange
            Account existing = TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(existing));
            given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
            AccountRequest update = TestFixtures.accountRequest(
                    "ACC-9001", AccountType.CURRENT, new BigDecimal("100.00"), 1L);

            // Act
            AccountResponse response = accountService.updateAccount(1L, update);

            // Assert
            assertThat(response.accountNumber()).isEqualTo("ACC-9001");
            assertThat(response.accountType()).isEqualTo(AccountType.CURRENT);
        }

        @Test
        @DisplayName("throws AccountNotFoundException when updating an unknown account")
        void updateAccount_whenMissing_throwsAccountNotFoundException() {
            // Arrange
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.updateAccount(99L, TestFixtures.accountRequest(1L)))
                    .isInstanceOf(AccountNotFoundException.class);
            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("deposit")
    class Deposit {

        @Test
        @DisplayName("increases the balance and records a DEPOSIT transaction")
        void deposit_withPositiveAmount_increasesBalanceAndRecordsTransaction() {
            // Arrange
            Account account = TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));
            given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
            given(transactionRepository.save(any(Transaction.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            TransactionResponse response = accountService.deposit(TestFixtures.depositRequest(1L, "50.00"));

            // Assert
            assertThat(account.getBalance()).isEqualByComparingTo("150.00");
            assertThat(response.transactionType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(response.amount()).isEqualByComparingTo("50.00");

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        }

        @Test
        @DisplayName("throws AccountNotFoundException when the account does not exist")
        void deposit_whenAccountMissing_throwsAccountNotFoundException() {
            // Arrange
            given(accountRepository.findById(99L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.deposit(TestFixtures.depositRequest(99L, "50.00")))
                    .isInstanceOf(AccountNotFoundException.class);
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("rejects a non-positive deposit amount")
        void deposit_withZeroAmount_throwsInvalidTransactionException() {
            // Arrange
            Account account = TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));

            // Act & Assert
            assertThatThrownBy(() -> accountService.deposit(TestFixtures.depositRequest(1L, "0.00")))
                    .isInstanceOf(InvalidTransactionException.class);
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("decreases the balance and records a WITHDRAWAL transaction")
        void withdraw_withSufficientFunds_decreasesBalanceAndRecordsTransaction() {
            // Arrange
            Account account = TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));
            given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
            given(transactionRepository.save(any(Transaction.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            TransactionResponse response = accountService.withdraw(TestFixtures.withdrawRequest(1L, "40.00"));

            // Assert
            assertThat(account.getBalance()).isEqualByComparingTo("60.00");
            assertThat(response.transactionType()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(response.amount()).isEqualByComparingTo("40.00");
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws InsufficientBalanceException when the amount exceeds the balance")
        void withdraw_whenAmountExceedsBalance_throwsInsufficientBalanceException() {
            // Arrange
            Account account = TestFixtures.savingsAccount(1L, new BigDecimal("30.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));

            // Act & Assert
            assertThatThrownBy(() -> accountService.withdraw(TestFixtures.withdrawRequest(1L, "40.00")))
                    .isInstanceOf(InsufficientBalanceException.class);
            assertThat(account.getBalance()).isEqualByComparingTo("30.00");
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("allows withdrawing the entire balance (edge case)")
        void withdraw_ofExactBalance_leavesZeroBalance() {
            // Arrange
            Account account = TestFixtures.savingsAccount(1L, new BigDecimal("75.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));
            given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
            given(transactionRepository.save(any(Transaction.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            accountService.withdraw(TestFixtures.withdrawRequest(1L, "75.00"));

            // Assert
            assertThat(account.getBalance()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("rejects a non-positive withdrawal amount")
        void withdraw_withNegativeAmount_throwsInvalidTransactionException() {
            // Arrange
            Account account = TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));

            // Act & Assert
            assertThatThrownBy(() -> accountService.withdraw(TestFixtures.withdrawRequest(1L, "-5.00")))
                    .isInstanceOf(InvalidTransactionException.class);
        }
    }

    @Nested
    @DisplayName("transfer")
    class Transfer {

        @Test
        @DisplayName("moves funds and records two transactions (debit + credit)")
        void transfer_withValidRequest_movesFundsAndRecordsTwoTransactions() {
            // Arrange
            Customer owner = TestFixtures.alice();
            Account source = TestFixtures.savingsAccount(1L, new BigDecimal("200.00"), owner);
            Account destination = TestFixtures.savingsAccount(2L, new BigDecimal("50.00"), owner);
            given(accountRepository.findById(1L)).willReturn(Optional.of(source));
            given(accountRepository.findById(2L)).willReturn(Optional.of(destination));
            given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
            given(transactionRepository.save(any(Transaction.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            List<TransactionResponse> transactions = accountService.transfer(
                    TestFixtures.transferRequest(1L, 2L, "80.00"));

            // Assert
            assertThat(source.getBalance()).isEqualByComparingTo("120.00");
            assertThat(destination.getBalance()).isEqualByComparingTo("130.00");
            assertThat(transactions).hasSize(2);
            verify(transactionRepository, times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws AccountNotFoundException when the source account does not exist")
        void transfer_whenSourceMissing_throwsAccountNotFoundException() {
            // Arrange
            given(accountRepository.findById(1L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.transfer(TestFixtures.transferRequest(1L, 2L, "80.00")))
                    .isInstanceOf(AccountNotFoundException.class);
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("throws AccountNotFoundException when the destination account does not exist")
        void transfer_whenDestinationMissing_throwsAccountNotFoundException() {
            // Arrange
            Account source = TestFixtures.savingsAccount(1L, new BigDecimal("200.00"), TestFixtures.alice());
            given(accountRepository.findById(1L)).willReturn(Optional.of(source));
            given(accountRepository.findById(2L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> accountService.transfer(TestFixtures.transferRequest(1L, 2L, "80.00")))
                    .isInstanceOf(AccountNotFoundException.class);
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("rejects a transfer to the same account")
        void transfer_toSameAccount_throwsInvalidTransactionException() {
            // Act & Assert
            assertThatThrownBy(() -> accountService.transfer(TestFixtures.transferRequest(1L, 1L, "80.00")))
                    .isInstanceOf(InvalidTransactionException.class);
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("is atomic: insufficient source funds leave both balances unchanged and record nothing")
        void transfer_withInsufficientSourceFunds_isAtomic() {
            // Arrange
            Customer owner = TestFixtures.alice();
            Account source = TestFixtures.savingsAccount(1L, new BigDecimal("30.00"), owner);
            Account destination = TestFixtures.savingsAccount(2L, new BigDecimal("50.00"), owner);
            given(accountRepository.findById(1L)).willReturn(Optional.of(source));
            given(accountRepository.findById(2L)).willReturn(Optional.of(destination));

            // Act & Assert
            assertThatThrownBy(() -> accountService.transfer(TestFixtures.transferRequest(1L, 2L, "80.00")))
                    .isInstanceOf(InsufficientBalanceException.class);
            assertThat(source.getBalance()).isEqualByComparingTo("30.00");
            assertThat(destination.getBalance()).isEqualByComparingTo("50.00");
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }
}
