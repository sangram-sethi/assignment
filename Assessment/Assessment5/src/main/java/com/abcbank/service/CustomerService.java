package com.abcbank.service;

import com.abcbank.dto.CustomerRequest;
import com.abcbank.dto.CustomerResponse;

import java.util.List;

/**
 * Customer domain operations. Behaviour is specified by {@code CustomerServiceTest}.
 */
public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse getCustomerById(Long id);

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    void deleteCustomer(Long id);
}
