package com.classroom.nbc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroom.nbc.config.AuthenticatedUserResolver;
import com.classroom.nbc.dto.request.EmiCalculationRequest;
import com.classroom.nbc.dto.request.LoanApplicationRequest;
import com.classroom.nbc.dto.response.EmiCalculationResponse;
import com.classroom.nbc.dto.response.ErrorResponse;
import com.classroom.nbc.dto.response.LoanApplicationResponse;
import com.classroom.nbc.enums.LoanType;
import com.classroom.nbc.service.LoanApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Loans", description = "Loan application operations for customers")
public class LoanController {

    private final LoanApplicationService loanApplicationService;
    private final AuthenticatedUserResolver userResolver;

    @PostMapping
    @Operation(summary = "Apply for a new loan",
            description = "Submits a loan application for the authenticated customer and returns the created "
                    + "application including its computed EMI schedule.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan application created"),
            @ApiResponse(responseCode = "400", description = "Invalid request or a pending application already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoanApplicationResponse> applyLoan(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody LoanApplicationRequest request) {
        Long customerId = userResolver.currentCustomerId(principal);
        log.info("POST /api/loans - customerId={} applying for {} loan of amount {}",
                customerId, request.getLoanType(), request.getLoanAmount());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanApplicationService.applyLoan(customerId, request));
    }

    @GetMapping
    @Operation(summary = "List the authenticated customer's loans")
    @ApiResponse(responseCode = "200", description = "Loans retrieved")
    public ResponseEntity<List<LoanApplicationResponse>> getMyLoans(
            @AuthenticationPrincipal UserDetails principal) {
        Long customerId = userResolver.currentCustomerId(principal);
        log.debug("GET /api/loans - listing loans for customerId={}", customerId);
        return ResponseEntity.ok(loanApplicationService.getCustomerLoans(customerId));
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "Get a loan application by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoanApplicationResponse> getLoan(
            @Parameter(description = "Identifier of the loan application", example = "1")
            @PathVariable Long loanId) {
        log.debug("GET /api/loans/{}", loanId);
        return ResponseEntity.ok(loanApplicationService.getLoanById(loanId));
    }

    @GetMapping("/types")
    @Operation(summary = "List the available loan types")
    @ApiResponse(responseCode = "200", description = "Loan types retrieved")
    public ResponseEntity<List<LoanType>> getLoanTypes() {
        log.debug("GET /api/loans/types");
        return ResponseEntity.ok(loanApplicationService.getAvailableLoanTypes());
    }

    @PostMapping("/calculate-emi")
    @Operation(summary = "Calculate the EMI for a loan without submitting an application",
            description = "Returns the monthly EMI, total payable and total interest for the given loan "
                    + "type, amount and tenure using the interest rate configured for the loan type.")
    @ApiResponse(responseCode = "200", description = "EMI calculated")
    public ResponseEntity<EmiCalculationResponse> calculateEmi(
            @Valid @RequestBody EmiCalculationRequest request) {
        log.debug("POST /api/loans/calculate-emi - {} loan, amount={}, tenureMonths={}",
                request.getLoanType(), request.getLoanAmount(), request.getTenureMonths());
        return ResponseEntity.ok(loanApplicationService.calculateEmi(request));
    }
}
