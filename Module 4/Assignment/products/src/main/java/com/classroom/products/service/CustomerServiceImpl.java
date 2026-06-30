package com.classroom.products.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;

import com.classroom.products.model.Customer;
import com.classroom.products.repository.CustomerRepository;
import com.classroom.products.dto.CustomerRequestDTO;
import com.classroom.products.dto.CustomerResponseDTO;
import com.classroom.products.exception.ResourceNotFoundException;

@Service
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    Customer mapRequestToEntityCustomer(CustomerRequestDTO customerRequestDTO) {
        Customer customer = new Customer();
        customer.setName(customerRequestDTO.getName());
        customer.setEmail(customerRequestDTO.getEmail());
        return customer;
    }

    CustomerResponseDTO mapEntityToResponseCustomer(Customer customer) {
        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setCustomerId(customer.getCustomerId());
        customerResponseDTO.setName(customer.getName());
        return customerResponseDTO;
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll(
            PageRequest.of(0, 5, Sort.by(Customer::getCustomerId))
        ).stream()
                .map(this::mapEntityToResponseCustomer)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public CustomerResponseDTO getCustomerById(Long id) {
        return mapEntityToResponseCustomer(findCustomerById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerResponseDTO saveCustomer(CustomerRequestDTO customer) {
        Customer savedCustomer = customerRepository.save(mapRequestToEntityCustomer(customer));
        return mapEntityToResponseCustomer(savedCustomer);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO customer) {
        Customer existingCustomer = findCustomerById(id);
        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        return mapEntityToResponseCustomer(customerRepository.save(existingCustomer));
    }
    
}