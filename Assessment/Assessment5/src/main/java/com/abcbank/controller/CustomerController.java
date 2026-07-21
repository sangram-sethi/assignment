package com.abcbank.controller;

import com.abcbank.dto.CustomerRequest;
import com.abcbank.dto.CustomerResponse;
import com.abcbank.service.CustomerService;
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
 * Customer CRUD endpoints. Handlers are skeletons pending implementation.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final String NOT_IMPLEMENTED = "Not implemented yet.";

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        // Intended: return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        // Intended: return ResponseEntity.ok(customerService.getAllCustomers());
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        // Intended: return ResponseEntity.ok(customerService.getCustomerById(id));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CustomerRequest request) {
        // Intended: return ResponseEntity.ok(customerService.updateCustomer(id, request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Intended: customerService.deleteCustomer(id); return ResponseEntity.noContent().build();
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
