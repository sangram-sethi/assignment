package com.classroom.nbc.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.classroom.nbc.exception.ResourceNotFoundException;
import com.classroom.nbc.model.Customer;
import com.classroom.nbc.model.User;
import com.classroom.nbc.repository.CustomerRepository;
import com.classroom.nbc.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Resolves the persistent identifiers for the currently authenticated principal.
 * The JWT subject is the username, which is mapped back to the {@link User} and
 * (for customers) the owning {@link Customer} record.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    /** Returns the {@code User.id} of the authenticated principal (used as approverId). */
    public Long currentUserId(UserDetails principal) {
        return currentUser(principal).getId();
    }

    /** Returns the {@code Customer.id} associated with the authenticated principal. */
    public Long currentCustomerId(UserDetails principal) {
        User user = currentUser(principal);
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No customer profile found for user " + user.getUsername()));
        return customer.getId();
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username " + principal.getUsername()));
    }
}
