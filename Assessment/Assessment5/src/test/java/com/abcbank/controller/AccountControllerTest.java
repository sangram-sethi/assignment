package com.abcbank.controller;

import com.abcbank.dto.AccountRequest;
import com.abcbank.dto.AccountResponse;
import com.abcbank.dto.DepositRequest;
import com.abcbank.dto.TransactionResponse;
import com.abcbank.dto.TransferRequest;
import com.abcbank.dto.WithdrawRequest;
import com.abcbank.entity.AccountType;
import com.abcbank.entity.TransactionType;
import com.abcbank.exception.AccountNotFoundException;
import com.abcbank.exception.InsufficientBalanceException;
import com.abcbank.service.AccountService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * Executable specification for {@link AccountController}, covering account CRUD
 * and the deposit / withdraw / transfer banking operations at the web layer.
 */
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AccountController web specification")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Nested
    @DisplayName("POST /api/accounts")
    class CreateAccount {

        @Test
        @DisplayName("returns 201 Created for a valid account request")
        void createAccount_withValidRequest_returns201() throws Exception {
            // Arrange
            AccountRequest request = TestFixtures.accountRequest(1L);
            given(accountService.createAccount(any(AccountRequest.class)))
                    .willReturn(new AccountResponse(10L, "ACC-1001", AccountType.SAVINGS, new BigDecimal("500.00"), 1L));

            // Act & Assert
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accountNumber").value("ACC-1001"))
                    .andExpect(jsonPath("$.accountType").value("SAVINGS"));
        }

        @Test
        @DisplayName("returns 400 Bad Request for a negative opening balance")
        void createAccount_withNegativeBalance_returns400() throws Exception {
            // Arrange
            AccountRequest request = TestFixtures.accountRequest(
                    "ACC-1001", AccountType.SAVINGS, new BigDecimal("-1.00"), 1L);

            // Act & Assert
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 Bad Request when the account type is not SAVINGS or CURRENT")
        void createAccount_withInvalidAccountType_returns400() throws Exception {
            // Arrange - an account type outside the allowed enum values
            String payload = "{\"accountNumber\":\"ACC-1001\",\"accountType\":\"PREMIUM\","
                    + "\"balance\":100.00,\"customerId\":1}";

            // Act & Assert
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 Not Found when the owning customer does not exist")
        void createAccount_whenCustomerMissing_returns404() throws Exception {
            // Arrange
            AccountRequest request = TestFixtures.accountRequest(99L);
            given(accountService.createAccount(any(AccountRequest.class)))
                    .willThrow(new AccountNotFoundException("customer missing"));

            // Act & Assert
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/accounts")
    class GetAccounts {

        @Test
        @DisplayName("returns 200 OK with all accounts")
        void getAllAccounts_returns200() throws Exception {
            // Arrange
            given(accountService.getAllAccounts()).willReturn(List.of(
                    new AccountResponse(1L, "ACC-1", AccountType.SAVINGS, new BigDecimal("100.00"), 1L)));

            // Act & Assert
            mockMvc.perform(get("/api/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("returns 404 Not Found for an unknown account id")
        void getAccountById_whenMissing_returns404() throws Exception {
            // Arrange
            given(accountService.getAccountById(99L)).willThrow(AccountNotFoundException.withId(99L));

            // Act & Assert
            mockMvc.perform(get("/api/accounts/{id}", 99L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/accounts/deposit")
    class Deposit {

        @Test
        @DisplayName("returns 200 OK and the created DEPOSIT transaction")
        void deposit_withValidRequest_returns200() throws Exception {
            // Arrange
            DepositRequest request = TestFixtures.depositRequest(1L, "50.00");
            given(accountService.deposit(any(DepositRequest.class)))
                    .willReturn(new TransactionResponse(1L, TransactionType.DEPOSIT, new BigDecimal("50.00"),
                            LocalDateTime.now(), "DEPOSIT", 1L));

            // Act & Assert
            mockMvc.perform(post("/api/accounts/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));
        }

        @Test
        @DisplayName("returns 400 Bad Request for a non-positive deposit amount")
        void deposit_withZeroAmount_returns400() throws Exception {
            // Arrange
            DepositRequest request = TestFixtures.depositRequest(1L, "0.00");

            // Act & Assert
            mockMvc.perform(post("/api/accounts/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/accounts/withdraw")
    class Withdraw {

        @Test
        @DisplayName("returns 400 Bad Request when the balance is insufficient")
        void withdraw_withInsufficientBalance_returns400() throws Exception {
            // Arrange
            WithdrawRequest request = TestFixtures.withdrawRequest(1L, "1000.00");
            given(accountService.withdraw(any(WithdrawRequest.class)))
                    .willThrow(new InsufficientBalanceException("Insufficient balance"));

            // Act & Assert
            mockMvc.perform(post("/api/accounts/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/accounts/transfer")
    class TransferFunds {

        @Test
        @DisplayName("returns 200 OK with the two transactions created by the transfer")
        void transfer_withValidRequest_returns200WithTwoTransactions() throws Exception {
            // Arrange
            TransferRequest request = TestFixtures.transferRequest(1L, 2L, "80.00");
            given(accountService.transfer(any(TransferRequest.class))).willReturn(List.of(
                    new TransactionResponse(1L, TransactionType.TRANSFER, new BigDecimal("80.00"),
                            LocalDateTime.now(), "TRANSFER OUT", 1L),
                    new TransactionResponse(2L, TransactionType.TRANSFER, new BigDecimal("80.00"),
                            LocalDateTime.now(), "TRANSFER IN", 2L)));

            // Act & Assert
            mockMvc.perform(post("/api/accounts/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("returns 400 Bad Request for a non-positive transfer amount")
        void transfer_withNegativeAmount_returns400() throws Exception {
            // Arrange
            TransferRequest request = TestFixtures.transferRequest(1L, 2L, "-10.00");

            // Act & Assert
            mockMvc.perform(post("/api/accounts/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/accounts/{id}")
    class UpdateAccount {

        @Test
        @DisplayName("returns 200 OK with the updated account")
        void updateAccount_whenExists_returns200() throws Exception {
            // Arrange
            AccountRequest request = TestFixtures.accountRequest(
                    "ACC-1001", AccountType.CURRENT, new BigDecimal("250.00"), 1L);
            given(accountService.updateAccount(eq(1L), any(AccountRequest.class)))
                    .willReturn(new AccountResponse(1L, "ACC-1001", AccountType.CURRENT, new BigDecimal("250.00"), 1L));

            // Act & Assert
            mockMvc.perform(put("/api/accounts/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountType").value("CURRENT"));
        }

        @Test
        @DisplayName("returns 404 Not Found when updating an unknown account")
        void updateAccount_whenMissing_returns404() throws Exception {
            // Arrange
            AccountRequest request = TestFixtures.accountRequest(1L);
            given(accountService.updateAccount(eq(99L), any(AccountRequest.class)))
                    .willThrow(AccountNotFoundException.withId(99L));

            // Act & Assert
            mockMvc.perform(put("/api/accounts/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/accounts/{id}")
    class DeleteAccount {

        @Test
        @DisplayName("returns 204 No Content when the account is deleted")
        void deleteAccount_whenExists_returns204() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/accounts/{id}", 1L))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 Not Found when deleting an unknown account")
        void deleteAccount_whenMissing_returns404() throws Exception {
            // Arrange
            willThrow(AccountNotFoundException.withId(99L)).given(accountService).deleteAccount(99L);

            // Act & Assert
            mockMvc.perform(delete("/api/accounts/{id}", 99L))
                    .andExpect(status().isNotFound());
        }
    }
}
