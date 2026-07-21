package com.abcbank.service;

import com.abcbank.dto.AuthResponse;
import com.abcbank.dto.LoginRequest;
import com.abcbank.dto.RegisterRequest;

/**
 * Authentication operations backing {@code /api/auth/**}.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
