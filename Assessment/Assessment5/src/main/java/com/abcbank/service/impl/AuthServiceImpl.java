package com.abcbank.service.impl;

import com.abcbank.dto.AuthResponse;
import com.abcbank.dto.LoginRequest;
import com.abcbank.dto.RegisterRequest;
import com.abcbank.repository.CustomerRepository;
import com.abcbank.security.JwtService;
import com.abcbank.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Skeleton implementation. The production logic is intentionally absent: this
 * project is delivered test-first. The registration/login behaviour is
 * specified by {@code SecurityTest} and {@code CustomerIntegrationTest}.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String NOT_IMPLEMENTED =
            "Not implemented yet - build against the authentication specification.";

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
