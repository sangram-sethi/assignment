package com.classroom.nbc.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A loan application including its computed EMI schedule")
public class LoanApplicationResponse {

    @Schema(description = "Loan application identifier", example = "1")
    private Long id;

    @Schema(description = "Type of loan", example = "HOME_LOAN")
    private String loanType;

    @Schema(description = "Requested principal amount", example = "500000")
    private BigDecimal loanAmount;

    @Schema(description = "Repayment tenure in months", example = "60")
    private Integer tenureMonths;

    @Schema(description = "Reason for the loan", example = "Home renovation")
    private String purpose;

    @Schema(description = "Annual interest rate applied, in percent", example = "8.50")
    private BigDecimal interestRate;

    @Schema(description = "Monthly instalment amount", example = "10258.27")
    private BigDecimal monthlyEmi;

    @Schema(description = "Total amount repayable over the tenure", example = "615496.20")
    private BigDecimal totalPayable;

    @Schema(description = "Total interest payable over the tenure", example = "115496.20")
    private BigDecimal totalInterest;

    @Schema(description = "Timestamp the application was submitted")
    private LocalDateTime applicationDate;

    @Schema(description = "Current status", example = "PENDING")
    private String status;

    @Schema(description = "Reviewer remarks, if processed")
    private String remarks;

    @Schema(description = "Username of the approver, if processed")
    private String approvedBy;
}
