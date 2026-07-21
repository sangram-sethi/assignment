package com.classroom.nbc.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroom.nbc.dto.request.LoanApprovalRequest;
import com.classroom.nbc.dto.response.LoanApplicationResponse;
import com.classroom.nbc.enums.LoanStatus;
import com.classroom.nbc.exception.LoanAlreadyProcessedException;
import com.classroom.nbc.exception.ResourceNotFoundException;
import com.classroom.nbc.mapper.LoanApplicationMapper;
import com.classroom.nbc.model.LoanApplication;
import com.classroom.nbc.model.User;
import com.classroom.nbc.repository.LoanApplicationRepository;
import com.classroom.nbc.repository.UserRepository;
import com.classroom.nbc.service.LoanApprovalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanApprovalServiceImpl implements LoanApprovalService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final LoanApplicationMapper loanApplicationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getPendingLoans() {
        log.debug("Fetching all pending loan applications");
        List<LoanApplicationResponse> pending = loanApplicationRepository.findByStatus(LoanStatus.PENDING)
                .stream()
                .map(loanApplicationMapper::toResponse)
                .toList();
        log.debug("Found {} pending loan application(s)", pending.size());
        return pending;
    }

    @Override
    @Transactional
    public LoanApplicationResponse approveOrReject(
            Long approverId, Long loanId, LoanApprovalRequest request) {

        log.info("Approver id={} is processing loanId={} with decision={}",
                approverId, loanId, request.getStatus());

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Approver not found with id " + approverId));

        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING) {
            log.warn("Cannot process loanId={}: already in status {}", loanId, loan.getStatus());
            throw new LoanAlreadyProcessedException(
                    "Loan application already " + loan.getStatus().name().toLowerCase());
        }

        loan.setStatus(request.getStatus());
        loan.setRemarks(request.getRemarks());
        loan.setApprovedBy(approver);

        LoanApplication savedLoan = loanApplicationRepository.save(loan);

        log.info("LoanId={} {} by approver id={}", loanId, savedLoan.getStatus(), approverId);

        return loanApplicationMapper.toResponse(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingLoanCount() {
        return loanApplicationRepository.findByStatus(LoanStatus.PENDING).size();
    }
}
