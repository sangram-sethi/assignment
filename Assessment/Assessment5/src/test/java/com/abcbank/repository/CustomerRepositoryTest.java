package com.abcbank.repository;

import com.abcbank.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Executable specification for {@link CustomerRepository} against a real
 * (in-memory H2) database, verifying the custom finders and the unique-email
 * constraint.
 */
@DataJpaTest
@DisplayName("CustomerRepository specification")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static Customer newCustomer(String name, String email) {
        return new Customer(null, name, email, "1234567890", "encoded-password");
    }

    @Test
    @DisplayName("persists a customer and finds it by email")
    void findByEmail_whenCustomerExists_returnsCustomer() {
        // Arrange
        entityManager.persistAndFlush(newCustomer("Alice", "alice@abcbank.com"));

        // Act
        Optional<Customer> found = customerRepository.findByEmail("alice@abcbank.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("returns empty when no customer matches the email")
    void findByEmail_whenCustomerMissing_returnsEmpty() {
        // Act
        Optional<Customer> found = customerRepository.findByEmail("nobody@abcbank.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("reports existence of an email")
    void existsByEmail_reflectsPersistedState() {
        // Arrange
        entityManager.persistAndFlush(newCustomer("Bob", "bob@abcbank.com"));

        // Act & Assert
        assertThat(customerRepository.existsByEmail("bob@abcbank.com")).isTrue();
        assertThat(customerRepository.existsByEmail("missing@abcbank.com")).isFalse();
    }

    @Test
    @DisplayName("enforces the unique-email constraint")
    void save_withDuplicateEmail_violatesUniqueConstraint() {
        // Arrange
        entityManager.persistAndFlush(newCustomer("Alice", "dup@abcbank.com"));
        Customer duplicate = newCustomer("Another Alice", "dup@abcbank.com");

        // Act & Assert
        assertThatThrownBy(() -> {
            customerRepository.saveAndFlush(duplicate);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("generates an id on persist and retrieves by id")
    void save_generatesIdAndIsRetrievableById() {
        // Arrange
        Customer saved = customerRepository.saveAndFlush(newCustomer("Carol", "carol@abcbank.com"));

        // Act
        Optional<Customer> found = customerRepository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("carol@abcbank.com");
    }
}
