package com.abcbank.controller;

import com.abcbank.dto.TransactionResponse;
import com.abcbank.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only transaction endpoints. Handlers are skeletons pending implementation.
 */
@RestController
public class TransactionController {

    private static final String NOT_IMPLEMENTED = "Not implemented yet.";

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/api/transactions")
    public ResponseEntity<List<TransactionResponse>> getAll() {
        // Intended: return ResponseEntity.ok(transactionService.getAllTransactions());
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @GetMapping("/api/transactions/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        // Intended: return ResponseEntity.ok(transactionService.getTransactionById(id));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @GetMapping("/api/accounts/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getByAccount(@PathVariable Long id) {
        // Intended: return ResponseEntity.ok(transactionService.getTransactionsByAccountId(id));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
