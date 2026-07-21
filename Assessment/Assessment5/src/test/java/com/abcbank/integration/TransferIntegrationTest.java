package com.abcbank.integration;

import com.abcbank.dto.AccountRequest;
import com.abcbank.dto.RegisterRequest;
import com.abcbank.dto.TransferRequest;
import com.abcbank.entity.AccountType;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end specification for a money transfer between two accounts, verifying
 * the atomic movement of funds and the creation of two transaction records
 * through the full HTTP + persistence stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Transfer end-to-end specification")
class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private String authorization;

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.deleteAll();
        authorization = registerAndGetBearerToken(new RegisterRequest("Alice", TestFixtures.VALID_EMAIL,
                TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD));
    }

    @Test
    @DisplayName("transfers funds atomically and records two transactions")
    void transfer_movesFundsAndRecordsTwoTransactions() throws Exception {
        // Arrange: one customer owning a source and a destination account
        long customerId = createCustomer("Holder", "holder@abcbank.com");
        long sourceId = createAccount("ACC-SRC", new BigDecimal("500.00"), customerId);
        long destinationId = createAccount("ACC-DST", new BigDecimal("100.00"), customerId);

        // Act: transfer 200 from source to destination
        TransferRequest transfer = new TransferRequest(sourceId, destinationId, new BigDecimal("200.00"));
        mockMvc.perform(post("/api/accounts/transfer")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Assert: balances reflect the transfer
        mockMvc.perform(get("/api/accounts/{id}", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300.00));

        mockMvc.perform(get("/api/accounts/{id}", destinationId)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300.00));

        // Assert: the source account has a transaction recorded against it
        mockMvc.perform(get("/api/accounts/{id}/transactions", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("rejects a transfer that exceeds the source balance and keeps balances unchanged")
    void transfer_withInsufficientFunds_returns400AndPreservesBalances() throws Exception {
        // Arrange
        long customerId = createCustomer("Holder", "holder@abcbank.com");
        long sourceId = createAccount("ACC-SRC", new BigDecimal("50.00"), customerId);
        long destinationId = createAccount("ACC-DST", new BigDecimal("0.00"), customerId);

        // Act: attempt to transfer more than is available
        TransferRequest transfer = new TransferRequest(sourceId, destinationId, new BigDecimal("500.00"));
        mockMvc.perform(post("/api/accounts/transfer")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest());

        // Assert: the source balance is untouched (atomic rollback)
        mockMvc.perform(get("/api/accounts/{id}", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    // ----- helpers ---------------------------------------------------------

    private String registerAndGetBearerToken(RegisterRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createCustomer(String name, String email) throws Exception {
        var request = TestFixtures.customerRequest(name, email, TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long createAccount(String number, BigDecimal balance, long customerId) throws Exception {
        AccountRequest request = new AccountRequest(number, AccountType.SAVINGS, balance, customerId);
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long readId(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }
}
