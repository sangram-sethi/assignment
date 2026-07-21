package com.abcbank.service;

import com.abcbank.dto.CustomerRequest;
import com.abcbank.dto.CustomerResponse;
import com.abcbank.entity.Customer;
import com.abcbank.exception.CustomerNotFoundException;
import com.abcbank.exception.DuplicateEmailException;
import com.abcbank.repository.CustomerRepository;
import com.abcbank.service.impl.CustomerServiceImpl;
import com.abcbank.testsupport.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Executable specification for the customer service layer.
 *
 * <p>Verifies the customer business rules: unique / mandatory fields, password
 * encryption, duplicate-registration conflict, and not-found handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService specification")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Nested
    @DisplayName("createCustomer")
    class CreateCustomer {

        @Test
        @DisplayName("persists the customer and returns a response when the email is unique")
        void createCustomer_withUniqueEmail_returnsPersistedCustomer() {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest();
            given(customerRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn(TestFixtures.ENCODED_PASSWORD);
            given(customerRepository.save(any(Customer.class)))
                    .willReturn(TestFixtures.customer(1L, request.name(), request.email()));

            // Act
            CustomerResponse response = customerService.createCustomer(request);

            // Assert
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Alice");
            assertThat(response.email()).isEqualTo(TestFixtures.VALID_EMAIL);
            assertThat(response.phone()).isEqualTo(TestFixtures.VALID_PHONE);
        }

        @Test
        @DisplayName("encrypts the password before persisting and never stores it in clear text")
        void createCustomer_encodesPasswordBeforePersisting() {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest();
            given(customerRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn(TestFixtures.ENCODED_PASSWORD);
            given(customerRepository.save(any(Customer.class)))
                    .willReturn(TestFixtures.customer(1L, request.name(), request.email()));

            // Act
            customerService.createCustomer(request);

            // Assert
            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword())
                    .isEqualTo(TestFixtures.ENCODED_PASSWORD)
                    .isNotEqualTo(request.password());
            verify(passwordEncoder).encode(request.password());
        }

        @Test
        @DisplayName("throws DuplicateEmailException and does not save when the email already exists")
        void createCustomer_withDuplicateEmail_throwsDuplicateEmailException() {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest();
            given(customerRepository.existsByEmail(request.email())).willReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> customerService.createCustomer(request))
                    .isInstanceOf(DuplicateEmailException.class);
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("getAllCustomers")
    class GetAllCustomers {

        @Test
        @DisplayName("returns every persisted customer mapped to a response")
        void getAllCustomers_returnsMappedResponses() {
            // Arrange
            given(customerRepository.findAll()).willReturn(List.of(
                    TestFixtures.customer(1L, "Alice", "alice@abcbank.com"),
                    TestFixtures.customer(2L, "Bob", "bob@abcbank.com")));

            // Act
            List<CustomerResponse> responses = customerService.getAllCustomers();

            // Assert
            assertThat(responses).hasSize(2)
                    .extracting(CustomerResponse::email)
                    .containsExactly("alice@abcbank.com", "bob@abcbank.com");
        }

        @Test
        @DisplayName("returns an empty list when no customers exist")
        void getAllCustomers_whenNoneExist_returnsEmptyList() {
            // Arrange
            given(customerRepository.findAll()).willReturn(List.of());

            // Act
            List<CustomerResponse> responses = customerService.getAllCustomers();

            // Assert
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCustomerById")
    class GetCustomerById {

        @Test
        @DisplayName("returns the customer when the id exists")
        void getCustomerById_whenExists_returnsCustomer() {
            // Arrange
            given(customerRepository.findById(1L)).willReturn(Optional.of(TestFixtures.alice()));

            // Act
            CustomerResponse response = customerService.getCustomerById(1L);

            // Assert
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo(TestFixtures.VALID_EMAIL);
        }

        @Test
        @DisplayName("throws CustomerNotFoundException when the id is unknown")
        void getCustomerById_whenMissing_throwsCustomerNotFoundException() {
            // Arrange
            given(customerRepository.findById(99L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> customerService.getCustomerById(99L))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateCustomer")
    class UpdateCustomer {

        @Test
        @DisplayName("updates and returns the customer when the id exists")
        void updateCustomer_whenExists_updatesFields() {
            // Arrange
            Customer existing = TestFixtures.alice();
            given(customerRepository.findById(1L)).willReturn(Optional.of(existing));
            given(customerRepository.save(any(Customer.class))).willAnswer(inv -> inv.getArgument(0));
            CustomerRequest update = TestFixtures.customerRequest("Alice Cooper", "alice.cooper@abcbank.com",
                    "9876543210", TestFixtures.RAW_PASSWORD);

            // Act
            CustomerResponse response = customerService.updateCustomer(1L, update);

            // Assert
            assertThat(response.name()).isEqualTo("Alice Cooper");
            assertThat(response.email()).isEqualTo("alice.cooper@abcbank.com");
            assertThat(response.phone()).isEqualTo("9876543210");
        }

        @Test
        @DisplayName("throws CustomerNotFoundException when updating an unknown id")
        void updateCustomer_whenMissing_throwsCustomerNotFoundException() {
            // Arrange
            given(customerRepository.findById(99L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> customerService.updateCustomer(99L, TestFixtures.customerRequest()))
                    .isInstanceOf(CustomerNotFoundException.class);
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("deleteCustomer")
    class DeleteCustomer {

        @Test
        @DisplayName("deletes the customer when the id exists")
        void deleteCustomer_whenExists_deletesCustomer() {
            // Arrange
            given(customerRepository.existsById(1L)).willReturn(true);

            // Act
            customerService.deleteCustomer(1L);

            // Assert
            verify(customerRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws CustomerNotFoundException when deleting an unknown id")
        void deleteCustomer_whenMissing_throwsCustomerNotFoundException() {
            // Arrange
            given(customerRepository.existsById(99L)).willReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                    .isInstanceOf(CustomerNotFoundException.class);
            verify(customerRepository, never()).deleteById(anyLong());
        }
    }
}
