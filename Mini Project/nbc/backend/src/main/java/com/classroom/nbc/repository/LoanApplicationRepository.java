package com.classroom.nbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroom.nbc.enums.LoanStatus;
import com.classroom.nbc.enums.LoanType;
import com.classroom.nbc.model.LoanApplication;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByCustomerId(Long customerId);

    List<LoanApplication> findByStatus(LoanStatus status);

    List<LoanApplication> findByCustomerIdAndStatus(Long customerId, LoanStatus status);

    List<LoanApplication> findByApprovedById(Long approverId);

    long countByCustomerId(Long customerId);

    long countByCustomerIdAndStatus(Long customerId, LoanStatus status);

    boolean existsByCustomerIdAndLoanTypeAndStatus(Long customerId, LoanType loanType, LoanStatus status);
}
