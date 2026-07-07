package org.northernarc.assessment4.controller;

import jakarta.validation.Valid;
import org.northernarc.assessment4.dto.AccountResponse;
import org.northernarc.assessment4.dto.CreateAccountRequest;
import org.northernarc.assessment4.dto.CreateCustomerRequest;
import org.northernarc.assessment4.dto.CustomerResponse;
import org.northernarc.assessment4.dto.CustomerSummaryDTO;
import org.northernarc.assessment4.dto.DashboardResponse;
import org.northernarc.assessment4.model.Account;
import org.northernarc.assessment4.model.Customer;
import org.northernarc.assessment4.service.BankService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // --- Registration endpoints (public) ---

    @PostMapping("/customers")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setCustomerName(request.customerName());
        customer.setEmail(request.email());
        customer.setPassword(request.password());
        customer.setBranch(request.branch());

        Customer saved = bankService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerResponse.from(saved));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(request.accountNumber());
        account.setAccountType(request.accountType());
        account.setBalance(request.balance());

        Account saved = bankService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(saved));
    }

    // --- Task 6: Pagination & sorting (balance DESC by default) ---

    @GetMapping("/accounts")
    public ResponseEntity<Page<AccountResponse>> getAccounts(
            @PageableDefault(size = 10, sort = "balance", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<AccountResponse> page = bankService.getAccounts(pageable).map(AccountResponse::from);
        return ResponseEntity.ok(page);
    }

    // --- Task 7: DTO projection ---

    @GetMapping("/customers/{customerId}/summary")
    public ResponseEntity<CustomerSummaryDTO> getCustomerSummary(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(bankService.getCustomerSummary(customerId));
    }

    // --- Task 9: Role-based authorization ---

    @DeleteMapping("/accounts/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        bankService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/accounts/{accountNumber}/balance")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AccountResponse> updateAccountBalance(
            @PathVariable String accountNumber,
            @RequestParam double amount) {
        Account updated = bankService.updateAccountBalance(accountNumber, amount);
        return ResponseEntity.ok(AccountResponse.from(updated));
    }

    // --- Final Challenge: dashboard analytics ---

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(bankService.getDashboard());
    }
}
