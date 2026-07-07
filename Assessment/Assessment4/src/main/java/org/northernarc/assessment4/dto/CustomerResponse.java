package org.northernarc.assessment4.dto;

import org.northernarc.assessment4.model.Customer;
import org.northernarc.assessment4.model.Role;

/**
 * Outbound representation of a customer. Never exposes the password hash or the
 * internal entity graph.
 */
public record CustomerResponse(
        Long customerId,
        String customerName,
        String email,
        String branch,
        Role role
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getBranch(),
                customer.getRole());
    }
}
