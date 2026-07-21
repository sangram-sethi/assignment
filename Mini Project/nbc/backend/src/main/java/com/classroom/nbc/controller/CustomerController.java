package com.classroom.nbc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroom.nbc.config.AuthenticatedUserResolver;
import com.classroom.nbc.dto.response.CustomerResponse;
import com.classroom.nbc.dto.response.LoanSummaryResponse;
import com.classroom.nbc.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Customer", description = "Customer profile and loan summary")
public class CustomerController {

    private final CustomerService customerService;
    private final AuthenticatedUserResolver userResolver;

    @GetMapping("/profile")
    @Operation(summary = "Get the authenticated customer's profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    public ResponseEntity<CustomerResponse> getProfile(
            @AuthenticationPrincipal UserDetails principal) {
        Long customerId = userResolver.currentCustomerId(principal);
        log.debug("GET /api/customers/profile - customerId={}", customerId);
        return ResponseEntity.ok(customerService.getProfile(customerId));
    }

    @GetMapping("/loan-summary")
    @Operation(summary = "Get a summary of the authenticated customer's loans")
    @ApiResponse(responseCode = "200", description = "Loan summary retrieved")
    public ResponseEntity<LoanSummaryResponse> getLoanSummary(
            @AuthenticationPrincipal UserDetails principal) {
        Long customerId = userResolver.currentCustomerId(principal);
        log.debug("GET /api/customers/loan-summary - customerId={}", customerId);
        return ResponseEntity.ok(customerService.getLoanSummary(customerId));
    }
}
