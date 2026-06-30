package com.classroom.products.service;

import com.classroom.products.dto.CustomerRequestDTO;
import com.classroom.products.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {
    
    CustomerResponseDTO saveCustomer(CustomerRequestDTO customer);
    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO customer);
    CustomerResponseDTO getCustomerById(Long id);
    List<CustomerResponseDTO> getAllCustomers();
    void deleteCustomer(Long id);

}
