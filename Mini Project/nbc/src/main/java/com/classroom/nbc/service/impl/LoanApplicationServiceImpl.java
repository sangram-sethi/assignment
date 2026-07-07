package com.classroom.nbc.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroom.nbc.dto.request.EmiCalculationRequest;
import com.classroom.nbc.dto.request.LoanApplicationRequest;
import com.classroom.nbc.dto.response.EmiCalculationResponse;
import com.classroom.nbc.dto.response.LoanApplicationResponse;
import com.classroom.nbc.enums.LoanStatus;
import com.classroom.nbc.enums.LoanType;
import com.classroom.nbc.exception.InvalidLoanApplicationException;
import com.classroom.nbc.exception.ResourceNotFoundException;
import com.classroom.nbc.mapper.LoanApplicationMapper;
import com.classroom.nbc.model.Customer;
import com.classroom.nbc.model.LoanApplication;
import com.classroom.nbc.repository.CustomerRepository;
import com.classroom.nbc.repository.LoanApplicationRepository;
import com.classroom.nbc.service.LoanApplicationService;
import com.classroom.nbc.util.EmiCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanApplicationMapper loanApplicationMapper;

    @Override
    @Transactional
    public LoanApplicationResponse applyLoan(Long customerId, LoanApplicationRequest request) {

        log.info("Processing loan application for customerId={}, loanType={}, amount={}, tenureMonths={}",
                customerId, request.getLoanType(), request.getLoanAmount(), request.getTenureMonths());

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id " + customerId));

        boolean hasPending = loanApplicationRepository
                .existsByCustomerIdAndLoanTypeAndStatus(
                        customerId, request.getLoanType(), LoanStatus.PENDING);

        if (hasPending) {
            log.warn("Rejected loan application: customerId={} already has a pending {} application",
                    customerId, request.getLoanType());
            throw new InvalidLoanApplicationException(
                    "Pending application already exists for this loan type");
        }

        BigDecimal annualInterestRate = request.getLoanType().getAnnualInterestRate();
        EmiCalculator.EmiResult emi = EmiCalculator.calculate(
                request.getLoanAmount(), annualInterestRate, request.getTenureMonths());

        LoanApplication loan = LoanApplication.builder()
                .customer(customer)
                .loanType(request.getLoanType())
                .loanAmount(request.getLoanAmount())
                .tenureMonths(request.getTenureMonths())
                .purpose(request.getPurpose())
                .interestRate(annualInterestRate)
                .monthlyEmi(emi.monthlyEmi())
                .totalPayable(emi.totalPayable())
                .totalInterest(emi.totalInterest())
                .status(LoanStatus.PENDING)
                .build();

        LoanApplication savedLoan = loanApplicationRepository.save(loan);

        log.info("Loan application created: loanId={}, customerId={}, monthlyEmi={}",
                savedLoan.getId(), customerId, savedLoan.getMonthlyEmi());

        return loanApplicationMapper.toResponse(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getCustomerLoans(Long customerId) {

        log.debug("Fetching loans for customerId={}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id " + customerId);
        }

        List<LoanApplicationResponse> loans = loanApplicationRepository.findByCustomerId(customerId)
                .stream()
                .map(loanApplicationMapper::toResponse)
                .toList();

        log.debug("Found {} loan(s) for customerId={}", loans.size(), customerId);
        return loans;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanById(Long loanId) {

        log.debug("Fetching loan by id={}", loanId);

        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id " + loanId));

        return loanApplicationMapper.toResponse(loan);
    }

    @Override
    public List<LoanType> getAvailableLoanTypes() {
        return Arrays.asList(LoanType.values());
    }

    @Override
    public EmiCalculationResponse calculateEmi(EmiCalculationRequest request) {
        log.debug("Calculating EMI preview for loanType={}, amount={}, tenureMonths={}",
                request.getLoanType(), request.getLoanAmount(), request.getTenureMonths());

        BigDecimal annualInterestRate = request.getLoanType().getAnnualInterestRate();
        EmiCalculator.EmiResult emi = EmiCalculator.calculate(
                request.getLoanAmount(), annualInterestRate, request.getTenureMonths());

        return EmiCalculationResponse.builder()
                .loanType(request.getLoanType().name())
                .loanAmount(request.getLoanAmount())
                .tenureMonths(request.getTenureMonths())
                .annualInterestRate(annualInterestRate)
                .monthlyEmi(emi.monthlyEmi())
                .totalPayable(emi.totalPayable())
                .totalInterest(emi.totalInterest())
                .build();
    }
}
