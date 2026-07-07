package org.northernarc.assessment4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for customer self-registration. Decouples the public API
 * contract from the JPA entity and prevents mass-assignment of sensitive or
 * server-owned fields (e.g. id, role, accounts).
 */
public record CreateCustomerRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a well-formed address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Branch is required")
        String branch
) {
}
