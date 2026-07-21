package com.abcbank.controller;

import com.abcbank.dto.AccountRequest;
import com.abcbank.dto.AccountResponse;
import com.abcbank.dto.DepositRequest;
import com.abcbank.dto.TransactionResponse;
import com.abcbank.dto.TransferRequest;
import com.abcbank.dto.WithdrawRequest;
import com.abcbank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Account CRUD plus banking operations (deposit / withdraw / transfer).
 * Handlers are skeletons pending implementation.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final String NOT_IMPLEMENTED = "Not implemented yet.";

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        // Intended: return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll() {
        // Intended: return ResponseEntity.ok(accountService.getAllAccounts());
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable Long id) {
        // Intended: return ResponseEntity.ok(accountService.getAccountById(id));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody AccountRequest request) {
        // Intended: return ResponseEntity.ok(accountService.updateAccount(id, request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Intended: accountService.deleteAccount(id); return ResponseEntity.noContent().build();
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        // Intended: return ResponseEntity.ok(accountService.deposit(request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        // Intended: return ResponseEntity.ok(accountService.withdraw(request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        // Intended: return ResponseEntity.ok(accountService.transfer(request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
