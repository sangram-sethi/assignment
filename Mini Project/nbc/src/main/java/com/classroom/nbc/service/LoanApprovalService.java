package com.classroom.nbc.service;

import java.util.List;

import com.classroom.nbc.dto.request.LoanApprovalRequest;
import com.classroom.nbc.dto.response.LoanApplicationResponse;

public interface LoanApprovalService {

    List<LoanApplicationResponse> getPendingLoans();

    LoanApplicationResponse approveOrReject(Long approverId, Long loanId, LoanApprovalRequest request);

    long getPendingLoanCount();
}
