package com.abcbank.controller;

import com.abcbank.dto.CustomerRequest;
import com.abcbank.dto.CustomerResponse;
import com.abcbank.exception.CustomerNotFoundException;
import com.abcbank.exception.DuplicateEmailException;
import com.abcbank.service.CustomerService;
import com.abcbank.testsupport.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Executable specification for {@link CustomerController} exercised through the
 * web layer with {@link MockMvc}. Security filters are disabled so that these
 * tests focus purely on request mapping, validation, status codes and payloads.
 */
@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CustomerController web specification")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Nested
    @DisplayName("POST /api/customers")
    class CreateCustomer {

        @Test
        @DisplayName("returns 201 Created with the persisted customer for a valid request")
        void createCustomer_withValidRequest_returns201() throws Exception {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest();
            given(customerService.createCustomer(any(CustomerRequest.class)))
                    .willReturn(new CustomerResponse(1L, "Alice", TestFixtures.VALID_EMAIL, TestFixtures.VALID_PHONE));

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value(TestFixtures.VALID_EMAIL));
        }

        @Test
        @DisplayName("returns 409 Conflict when the email is already registered")
        void createCustomer_withDuplicateEmail_returns409() throws Exception {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest();
            given(customerService.createCustomer(any(CustomerRequest.class)))
                    .willThrow(DuplicateEmailException.forEmail(request.email()));

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("returns 400 Bad Request when the email is invalid")
        void createCustomer_withInvalidEmail_returns400() throws Exception {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest("Alice", "not-an-email",
                    TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 Bad Request when the name is blank")
        void createCustomer_withBlankName_returns400() throws Exception {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest("", TestFixtures.VALID_EMAIL,
                    TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 Bad Request when the phone number is not exactly 10 digits")
        void createCustomer_withInvalidPhone_returns400() throws Exception {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest("Alice", TestFixtures.VALID_EMAIL,
                    "12345", TestFixtures.RAW_PASSWORD);

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/customers")
    class GetCustomers {

        @Test
        @DisplayName("returns 200 OK with all customers")
        void getAllCustomers_returns200WithList() throws Exception {
            // Arrange
            given(customerService.getAllCustomers()).willReturn(List.of(
                    new CustomerResponse(1L, "Alice", "alice@abcbank.com", TestFixtures.VALID_PHONE),
                    new CustomerResponse(2L, "Bob", "bob@abcbank.com", TestFixtures.VALID_PHONE)));

            // Act & Assert
            mockMvc.perform(get("/api/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].email").value("alice@abcbank.com"));
        }

        @Test
        @DisplayName("returns 200 OK with the requested customer")
        void getCustomerById_whenExists_returns200() throws Exception {
            // Arrange
            given(customerService.getCustomerById(1L))
                    .willReturn(new CustomerResponse(1L, "Alice", TestFixtures.VALID_EMAIL, TestFixtures.VALID_PHONE));

            // Act & Assert
            mockMvc.perform(get("/api/customers/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("returns 404 Not Found when the customer does not exist")
        void getCustomerById_whenMissing_returns404() throws Exception {
            // Arrange
            given(customerService.getCustomerById(99L)).willThrow(CustomerNotFoundException.withId(99L));

            // Act & Assert
            mockMvc.perform(get("/api/customers/{id}", 99L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/customers/{id}")
    class UpdateCustomer {

        @Test
        @DisplayName("returns 200 OK with the updated customer")
        void updateCustomer_whenExists_returns200() throws Exception {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest("Alice Cooper", "alice.cooper@abcbank.com",
                    "9876543210", TestFixtures.RAW_PASSWORD);
            given(customerService.updateCustomer(eq(1L), any(CustomerRequest.class)))
                    .willReturn(new CustomerResponse(1L, "Alice Cooper", "alice.cooper@abcbank.com", "9876543210"));

            // Act & Assert
            mockMvc.perform(put("/api/customers/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Alice Cooper"));
        }

        @Test
        @DisplayName("returns 404 Not Found when updating an unknown customer")
        void updateCustomer_whenMissing_returns404() throws Exception {
            // Arrange
            CustomerRequest request = TestFixtures.customerRequest();
            given(customerService.updateCustomer(eq(99L), any(CustomerRequest.class)))
                    .willThrow(CustomerNotFoundException.withId(99L));

            // Act & Assert
            mockMvc.perform(put("/api/customers/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/customers/{id}")
    class DeleteCustomer {

        @Test
        @DisplayName("returns 204 No Content when the customer is deleted")
        void deleteCustomer_whenExists_returns204() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/customers/{id}", 1L))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 Not Found when deleting an unknown customer")
        void deleteCustomer_whenMissing_returns404() throws Exception {
            // Arrange
            willThrow(CustomerNotFoundException.withId(99L)).given(customerService).deleteCustomer(99L);

            // Act & Assert
            mockMvc.perform(delete("/api/customers/{id}", 99L))
                    .andExpect(status().isNotFound());
        }
    }
}
