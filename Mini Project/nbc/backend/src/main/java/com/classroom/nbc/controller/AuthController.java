package com.classroom.nbc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroom.nbc.dto.request.LoginRequest;
import com.classroom.nbc.dto.request.RegisterCustomerRequest;
import com.classroom.nbc.dto.response.CustomerResponse;
import com.classroom.nbc.dto.response.ErrorResponse;
import com.classroom.nbc.dto.response.JwtResponse;
import com.classroom.nbc.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Customer registration and login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer",
            description = "Creates a customer profile together with its login account and returns the created customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or username/email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerResponse> register(
            @Valid @RequestBody RegisterCustomerRequest request) {
        log.info("POST /api/auth/register - registering username={}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerCustomer(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain a JWT token",
            description = "Validates the supplied credentials and returns a signed JWT for use as a Bearer token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - login attempt for username={}", request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }
}
