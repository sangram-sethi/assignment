package com.classroom.nbc.mapper;

import org.springframework.stereotype.Component;

import com.classroom.nbc.dto.response.LoanApplicationResponse;
import com.classroom.nbc.model.LoanApplication;

/**
 * Maps {@link LoanApplication} entities to their API response representation.
 * Centralised here so controllers and services share a single mapping definition.
 */
@Component
public class LoanApplicationMapper {

    public LoanApplicationResponse toResponse(LoanApplication loan) {
        if (loan == null) {
            return null;
        }
        return LoanApplicationResponse.builder()
                .id(loan.getId())
                .loanType(loan.getLoanType() != null ? loan.getLoanType().name() : null)
                .loanAmount(loan.getLoanAmount())
                .tenureMonths(loan.getTenureMonths())
                .purpose(loan.getPurpose())
                .interestRate(loan.getInterestRate())
                .monthlyEmi(loan.getMonthlyEmi())
                .totalPayable(loan.getTotalPayable())
                .totalInterest(loan.getTotalInterest())
                .applicationDate(loan.getApplicationDate())
                .status(loan.getStatus() != null ? loan.getStatus().name() : null)
                .remarks(loan.getRemarks())
                .approvedBy(loan.getApprovedBy() != null ? loan.getApprovedBy().getUsername() : null)
                .build();
    }
}
