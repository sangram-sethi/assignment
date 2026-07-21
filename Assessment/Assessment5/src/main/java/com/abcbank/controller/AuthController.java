package com.abcbank.controller;

import com.abcbank.dto.AuthResponse;
import com.abcbank.dto.LoginRequest;
import com.abcbank.dto.RegisterRequest;
import com.abcbank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints. Handlers are skeletons pending implementation;
 * bean-validation still runs, so malformed requests are rejected with 400.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String NOT_IMPLEMENTED = "Not implemented yet.";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Intended: return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Intended: return ResponseEntity.ok(authService.login(request));
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
