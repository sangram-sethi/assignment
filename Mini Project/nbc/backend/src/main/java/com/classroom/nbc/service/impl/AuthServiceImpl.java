package com.classroom.nbc.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroom.nbc.config.JwtService;
import com.classroom.nbc.dto.request.LoginRequest;
import com.classroom.nbc.dto.request.RegisterCustomerRequest;
import com.classroom.nbc.dto.response.CustomerResponse;
import com.classroom.nbc.dto.response.JwtResponse;
import com.classroom.nbc.enums.Role;
import com.classroom.nbc.exception.InvalidLoanApplicationException;
import com.classroom.nbc.exception.ResourceNotFoundException;
import com.classroom.nbc.mapper.CustomerMapper;
import com.classroom.nbc.model.Customer;
import com.classroom.nbc.model.User;
import com.classroom.nbc.repository.CustomerRepository;
import com.classroom.nbc.repository.UserRepository;
import com.classroom.nbc.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(RegisterCustomerRequest request) {

        log.info("Registering new customer with username={}, email={}",
                request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username '{}' already exists", request.getUsername());
            throw new InvalidLoanApplicationException(
                    "Username already exists: " + request.getUsername());
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email '{}' already exists", request.getEmail());
            throw new InvalidLoanApplicationException(
                    "Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .annualIncome(request.getAnnualIncome())
                .user(user)
                .build();

        // Customer is the owning side with cascade = ALL, so the User is persisted too.
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer registered successfully: customerId={}, username={}",
                savedCustomer.getId(), request.getUsername());

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {

        log.info("Login attempt for username={}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username " + request.getUsername()));

        log.info("Login successful for username={}, role={}", user.getUsername(), user.getRole());
        return new JwtResponse(token, user.getUsername(), user.getRole().name());
    }
}
