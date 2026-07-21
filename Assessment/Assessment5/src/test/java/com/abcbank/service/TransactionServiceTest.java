package com.abcbank.service;

import com.abcbank.dto.TransactionResponse;
import com.abcbank.entity.Account;
import com.abcbank.entity.TransactionType;
import com.abcbank.exception.AccountNotFoundException;
import com.abcbank.repository.AccountRepository;
import com.abcbank.repository.TransactionRepository;
import com.abcbank.service.impl.TransactionServiceImpl;
import com.abcbank.testsupport.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * Executable specification for the read-only transaction service.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService specification")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final Account account = TestFixtures.savingsAccount(1L, new BigDecimal("100.00"), TestFixtures.alice());

    @Nested
    @DisplayName("getAllTransactions")
    class GetAllTransactions {

        @Test
        @DisplayName("returns all transactions mapped to responses")
        void getAllTransactions_returnsMappedResponses() {
            // Arrange
            given(transactionRepository.findAll()).willReturn(List.of(
                    TestFixtures.transaction(1L, TransactionType.DEPOSIT, "100.00", account),
                    TestFixtures.transaction(2L, TransactionType.WITHDRAWAL, "40.00", account)));

            // Act
            List<TransactionResponse> responses = transactionService.getAllTransactions();

            // Assert
            assertThat(responses).hasSize(2)
                    .extracting(TransactionResponse::transactionType)
                    .containsExactly(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL);
        }

        @Test
        @DisplayName("returns an empty list when there are no transactions")
        void getAllTransactions_whenNoneExist_returnsEmptyList() {
            // Arrange
            given(transactionRepository.findAll()).willReturn(List.of());

            // Act & Assert
            assertThat(transactionService.getAllTransactions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTransactionById")
    class GetTransactionById {

        @Test
        @DisplayName("returns the transaction when the id exists")
        void getTransactionById_whenExists_returnsTransaction() {
            // Arrange
            given(transactionRepository.findById(1L)).willReturn(
                    Optional.of(TestFixtures.transaction(1L, TransactionType.DEPOSIT, "100.00", account)));

            // Act
            TransactionResponse response = transactionService.getTransactionById(1L);

            // Assert
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.transactionType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(response.amount()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("throws when the transaction id is unknown")
        void getTransactionById_whenMissing_throwsException() {
            // Arrange
            given(transactionRepository.findById(99L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getTransactionsByAccountId")
    class GetTransactionsByAccountId {

        @Test
        @DisplayName("returns the transactions belonging to an existing account")
        void getTransactionsByAccountId_whenAccountExists_returnsTransactions() {
            // Arrange
            given(accountRepository.existsById(1L)).willReturn(true);
            given(transactionRepository.findByAccountId(1L)).willReturn(List.of(
                    TestFixtures.transaction(1L, TransactionType.DEPOSIT, "100.00", account)));

            // Act
            List<TransactionResponse> responses = transactionService.getTransactionsByAccountId(1L);

            // Assert
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).accountId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("throws AccountNotFoundException when the account does not exist")
        void getTransactionsByAccountId_whenAccountMissing_throwsAccountNotFoundException() {
            // Arrange
            given(accountRepository.existsById(99L)).willReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> transactionService.getTransactionsByAccountId(99L))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }
}
