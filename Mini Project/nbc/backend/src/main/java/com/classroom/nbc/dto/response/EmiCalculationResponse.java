package com.classroom.nbc.dto.response;

import java.math.BigDecimal;

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
@Schema(description = "Computed EMI breakdown for a prospective loan")
public class EmiCalculationResponse {

    @Schema(description = "Type of loan", example = "HOME_LOAN")
    private String loanType;

    @Schema(description = "Principal amount", example = "500000")
    private BigDecimal loanAmount;

    @Schema(description = "Repayment tenure in months", example = "60")
    private Integer tenureMonths;

    @Schema(description = "Annual interest rate applied, in percent", example = "8.50")
    private BigDecimal annualInterestRate;

    @Schema(description = "Monthly instalment amount", example = "10258.27")
    private BigDecimal monthlyEmi;

    @Schema(description = "Total amount repayable over the tenure", example = "615496.20")
    private BigDecimal totalPayable;

    @Schema(description = "Total interest payable over the tenure", example = "115496.20")
    private BigDecimal totalInterest;
}
