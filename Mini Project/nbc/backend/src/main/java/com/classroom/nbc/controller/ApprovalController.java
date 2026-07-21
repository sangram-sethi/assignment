package com.classroom.nbc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroom.nbc.config.AuthenticatedUserResolver;
import com.classroom.nbc.dto.request.LoanApprovalRequest;
import com.classroom.nbc.dto.response.ErrorResponse;
import com.classroom.nbc.dto.response.LoanApplicationResponse;
import com.classroom.nbc.service.LoanApprovalService;

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
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Loan Approvals", description = "Loan review operations for approvers")
public class ApprovalController {

    private final LoanApprovalService loanApprovalService;
    private final AuthenticatedUserResolver userResolver;

    @GetMapping("/pending")
    @Operation(summary = "List all pending loan applications")
    @ApiResponse(responseCode = "200", description = "Pending loans retrieved")
    public ResponseEntity<List<LoanApplicationResponse>> getPendingLoans() {
        log.debug("GET /api/approvals/pending");
        return ResponseEntity.ok(loanApprovalService.getPendingLoans());
    }

    @GetMapping("/pending/count")
    @Operation(summary = "Get the count of pending loan applications")
    @ApiResponse(responseCode = "200", description = "Pending count retrieved")
    public ResponseEntity<Long> getPendingLoanCount() {
        log.debug("GET /api/approvals/pending/count");
        return ResponseEntity.ok(loanApprovalService.getPendingLoanCount());
    }

    @PutMapping("/{loanId}")
    @Operation(summary = "Approve or reject a loan application",
            description = "Records an approval decision for the given pending loan. Only pending loans can be processed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decision recorded"),
            @ApiResponse(responseCode = "404", description = "Loan or approver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Loan has already been processed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoanApplicationResponse> approveOrReject(
            @AuthenticationPrincipal UserDetails principal,
            @Parameter(description = "Identifier of the loan application to process", example = "1")
            @PathVariable Long loanId,
            @Valid @RequestBody LoanApprovalRequest request) {
        Long approverId = userResolver.currentUserId(principal);
        log.info("PUT /api/approvals/{} - approverId={} decision={}", loanId, approverId, request.getStatus());
        return ResponseEntity.ok(
                loanApprovalService.approveOrReject(approverId, loanId, request));
    }
}
