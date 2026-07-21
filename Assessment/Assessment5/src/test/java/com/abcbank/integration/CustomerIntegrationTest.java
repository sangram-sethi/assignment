package com.abcbank.integration;

import com.abcbank.dto.CustomerRequest;
import com.abcbank.dto.RegisterRequest;
import com.abcbank.repository.CustomerRepository;
import com.abcbank.testsupport.TestFixtures;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end specification for the customer lifecycle exercised through the full
 * HTTP stack (security + controller + service + repository + H2).
 *
 * <p>A caller registers, obtains a JWT, and then creates, reads, updates and
 * deletes a customer using that token.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Customer end-to-end specification")
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("supports the full register -> create -> read -> update -> delete journey")
    void fullCustomerLifecycle() throws Exception {
        // Arrange: register the acting user and obtain a bearer token
        String authorization = registerAndGetBearerToken(
                new RegisterRequest("Alice", TestFixtures.VALID_EMAIL, TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD));

        // Act & Assert: create a customer
        CustomerRequest createRequest = new CustomerRequest("Bob", "bob@abcbank.com",
                TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD);
        MvcResult created = mockMvc.perform(post("/api/customers")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("bob@abcbank.com"))
                .andReturn();
        long customerId = extractId(created);

        // read
        mockMvc.perform(get("/api/customers/{id}", customerId)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"));

        // update
        CustomerRequest updateRequest = new CustomerRequest("Bobby", "bobby@abcbank.com",
                "9876543210", TestFixtures.RAW_PASSWORD);
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bobby"));

        // delete
        mockMvc.perform(delete("/api/customers/{id}", customerId)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNoContent());

        // confirm deletion
        mockMvc.perform(get("/api/customers/{id}", customerId)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("rejects a duplicate registration with 409 Conflict")
    void duplicateRegistration_returns409() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("Alice", TestFixtures.VALID_EMAIL,
                TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD);
        registerAndGetBearerToken(request);

        // Act & Assert: registering the same email again conflicts
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ----- helpers ---------------------------------------------------------

    private String registerAndGetBearerToken(RegisterRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return "Bearer " + body.get("token").asText();
    }

    private long extractId(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }
}
