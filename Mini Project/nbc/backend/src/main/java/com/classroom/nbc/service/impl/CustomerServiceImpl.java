package com.classroom.nbc.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroom.nbc.dto.response.CustomerResponse;
import com.classroom.nbc.dto.response.LoanSummaryResponse;
import com.classroom.nbc.enums.LoanStatus;
import com.classroom.nbc.exception.ResourceNotFoundException;
import com.classroom.nbc.mapper.CustomerMapper;
import com.classroom.nbc.model.Customer;
import com.classroom.nbc.repository.CustomerRepository;
import com.classroom.nbc.repository.LoanApplicationRepository;
import com.classroom.nbc.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getProfile(Long customerId) {

        log.debug("Fetching profile for customerId={}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id " + customerId));

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanSummaryResponse getLoanSummary(Long customerId) {

        log.debug("Building loan summary for customerId={}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id " + customerId);
        }

        long total = loanApplicationRepository.countByCustomerId(customerId);
        long approved = loanApplicationRepository
                .countByCustomerIdAndStatus(customerId, LoanStatus.APPROVED);
        long rejected = loanApplicationRepository
                .countByCustomerIdAndStatus(customerId, LoanStatus.REJECTED);
        long pending = loanApplicationRepository
                .countByCustomerIdAndStatus(customerId, LoanStatus.PENDING);

        return LoanSummaryResponse.builder()
                .totalApplications(total)
                .approvedApplications(approved)
                .rejectedApplications(rejected)
                .pendingApplications(pending)
                .build();
    }
}
