package com.abcbank.repository;

import com.abcbank.entity.Account;
import com.abcbank.entity.AccountType;
import com.abcbank.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Executable specification for {@link AccountRepository} against an in-memory H2
 * database, verifying the custom finders and the unique account-number constraint.
 */
@DataJpaTest
@DisplayName("AccountRepository specification")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer owner;

    @BeforeEach
    void setUp() {
        owner = entityManager.persistAndFlush(
                new Customer(null, "Alice", "alice@abcbank.com", "1234567890", "encoded-password"));
    }

    private Account newAccount(String number, AccountType type, BigDecimal balance) {
        return new Account(null, number, type, balance, owner);
    }

    @Test
    @DisplayName("persists an account and finds it by account number")
    void findByAccountNumber_whenExists_returnsAccount() {
        // Arrange
        entityManager.persistAndFlush(newAccount("ACC-1001", AccountType.SAVINGS, new BigDecimal("100.00")));

        // Act
        Optional<Account> found = accountRepository.findByAccountNumber("ACC-1001");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(found.get().getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("reports existence of an account number")
    void existsByAccountNumber_reflectsPersistedState() {
        // Arrange
        entityManager.persistAndFlush(newAccount("ACC-2002", AccountType.CURRENT, new BigDecimal("0.00")));

        // Act & Assert
        assertThat(accountRepository.existsByAccountNumber("ACC-2002")).isTrue();
        assertThat(accountRepository.existsByAccountNumber("ACC-9999")).isFalse();
    }

    @Test
    @DisplayName("returns all accounts belonging to a customer")
    void findByCustomerId_returnsOnlyThatCustomersAccounts() {
        // Arrange
        entityManager.persistAndFlush(newAccount("ACC-1", AccountType.SAVINGS, new BigDecimal("10.00")));
        entityManager.persistAndFlush(newAccount("ACC-2", AccountType.CURRENT, new BigDecimal("20.00")));
        Customer other = entityManager.persistAndFlush(
                new Customer(null, "Bob", "bob@abcbank.com", "1234567890", "encoded-password"));
        entityManager.persistAndFlush(new Account(null, "ACC-3", AccountType.SAVINGS, new BigDecimal("30.00"), other));

        // Act
        List<Account> aliceAccounts = accountRepository.findByCustomerId(owner.getId());

        // Assert
        assertThat(aliceAccounts).hasSize(2)
                .extracting(Account::getAccountNumber)
                .containsExactlyInAnyOrder("ACC-1", "ACC-2");
    }

    @Test
    @DisplayName("enforces the unique account-number constraint")
    void save_withDuplicateAccountNumber_violatesUniqueConstraint() {
        // Arrange
        entityManager.persistAndFlush(newAccount("ACC-DUP", AccountType.SAVINGS, new BigDecimal("10.00")));
        Account duplicate = newAccount("ACC-DUP", AccountType.CURRENT, new BigDecimal("20.00"));

        // Act & Assert
        assertThatThrownBy(() -> accountRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class);
    }
}
