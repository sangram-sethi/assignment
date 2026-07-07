package org.northernarc.assessment4.security;

import org.northernarc.assessment4.model.Customer;
import org.northernarc.assessment4.model.Role;
import org.northernarc.assessment4.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads customer credentials for Spring Security authentication, mapping the
 * customer's persisted {@link Role} onto their granted authorities.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No customer registered with email: " + email));

        Role role = customer.getRole() != null ? customer.getRole() : Role.USER;

        return User.withUsername(customer.getEmail())
                .password(customer.getPassword())
                .roles(role.name())
                .build();
    }
}
