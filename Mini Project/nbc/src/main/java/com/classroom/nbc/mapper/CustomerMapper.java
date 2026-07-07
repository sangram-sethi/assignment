package com.classroom.nbc.mapper;

import org.springframework.stereotype.Component;

import com.classroom.nbc.dto.response.CustomerResponse;
import com.classroom.nbc.model.Customer;

/**
 * Maps {@link Customer} entities to their API response representation.
 * Centralised here so the auth and customer services share a single mapping definition.
 */
@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .annualIncome(customer.getAnnualIncome())
                .build();
    }
}
