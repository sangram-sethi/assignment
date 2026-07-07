package com.classroom.nbc.service;

import java.util.List;

import com.classroom.nbc.dto.request.EmiCalculationRequest;
import com.classroom.nbc.dto.request.LoanApplicationRequest;
import com.classroom.nbc.dto.response.EmiCalculationResponse;
import com.classroom.nbc.dto.response.LoanApplicationResponse;
import com.classroom.nbc.enums.LoanType;

public interface LoanApplicationService {

    LoanApplicationResponse applyLoan(Long customerId, LoanApplicationRequest request);

    List<LoanApplicationResponse> getCustomerLoans(Long customerId);

    LoanApplicationResponse getLoanById(Long loanId);

    List<LoanType> getAvailableLoanTypes();

    EmiCalculationResponse calculateEmi(EmiCalculationRequest request);
}
