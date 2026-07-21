package com.abcbank.service.impl;

import com.abcbank.dto.CustomerRequest;
import com.abcbank.dto.CustomerResponse;
import com.abcbank.repository.CustomerRepository;
import com.abcbank.service.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Skeleton implementation. The production logic is intentionally absent: this
 * project is delivered test-first, and {@code CustomerServiceTest} is the
 * executable specification that this class must be built to satisfy.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String NOT_IMPLEMENTED =
            "Not implemented yet - build against the CustomerServiceTest specification.";

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void deleteCustomer(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
