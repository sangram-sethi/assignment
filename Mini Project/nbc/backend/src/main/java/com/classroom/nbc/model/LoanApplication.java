package com.classroom.nbc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.classroom.nbc.enums.LoanStatus;
import com.classroom.nbc.enums.LoanType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;

    @Positive(message = "Loan amount must be positive")
    @NotNull(message = "Loan amount is required")
    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Positive(message = "Tenure months must be positive")
    @NotNull(message = "Tenure months is required")
    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @NotBlank(message = "Purpose cannot be blank")
    @Column(nullable = false)
    private String purpose;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "monthly_emi")
    private BigDecimal monthlyEmi;

    @Column(name = "total_payable")
    private BigDecimal totalPayable;

    @Column(name = "total_interest")
    private BigDecimal totalInterest;

    @Column(name = "application_date", nullable = false, updatable = false)
    private LocalDateTime applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column
    private String remarks;

    // Many LoanApplications belong to one Customer.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference("customer-loans")
    private Customer customer;

    // Many LoanApplications can be approved by one User (Loan Approver).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @PrePersist
    protected void onCreate() {
        this.applicationDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = LoanStatus.PENDING;
        }
    }
}
