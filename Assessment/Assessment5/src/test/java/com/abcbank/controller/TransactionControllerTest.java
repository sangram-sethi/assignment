package com.abcbank.controller;

import com.abcbank.dto.TransactionResponse;
import com.abcbank.entity.TransactionType;
import com.abcbank.exception.AccountNotFoundException;
import com.abcbank.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Executable specification for {@link TransactionController} — the read-only
 * transaction query endpoints.
 */
@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TransactionController web specification")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private TransactionResponse sampleTransaction(Long id, Long accountId) {
        return new TransactionResponse(id, TransactionType.DEPOSIT, new BigDecimal("100.00"),
                LocalDateTime.now(), "DEPOSIT", accountId);
    }

    @Nested
    @DisplayName("GET /api/transactions")
    class GetAllTransactions {

        @Test
        @DisplayName("returns 200 OK with all transactions")
        void getAllTransactions_returns200() throws Exception {
            // Arrange
            given(transactionService.getAllTransactions())
                    .willReturn(List.of(sampleTransaction(1L, 1L), sampleTransaction(2L, 1L)));

            // Act & Assert
            mockMvc.perform(get("/api/transactions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/{id}")
    class GetTransactionById {

        @Test
        @DisplayName("returns 200 OK with the requested transaction")
        void getTransactionById_whenExists_returns200() throws Exception {
            // Arrange
            given(transactionService.getTransactionById(1L)).willReturn(sampleTransaction(1L, 1L));

            // Act & Assert
            mockMvc.perform(get("/api/transactions/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("returns 404 Not Found for an unknown transaction id")
        void getTransactionById_whenMissing_returns404() throws Exception {
            // Arrange
            given(transactionService.getTransactionById(99L))
                    .willThrow(new AccountNotFoundException("transaction 99 not found"));

            // Act & Assert
            mockMvc.perform(get("/api/transactions/{id}", 99L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/accounts/{id}/transactions")
    class GetTransactionsByAccount {

        @Test
        @DisplayName("returns 200 OK with the account's transaction history")
        void getTransactionsByAccount_returns200() throws Exception {
            // Arrange
            given(transactionService.getTransactionsByAccountId(1L))
                    .willReturn(List.of(sampleTransaction(1L, 1L)));

            // Act & Assert
            mockMvc.perform(get("/api/accounts/{id}/transactions", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].accountId").value(1));
        }
    }
}
