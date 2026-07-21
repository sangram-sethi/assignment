package com.classroom.nbc.dto.request;

import java.math.BigDecimal;

import com.classroom.nbc.enums.LoanType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Payload to preview the EMI for a prospective loan")
public class EmiCalculationRequest {

    @Schema(description = "Type of loan whose interest rate should be used", example = "HOME_LOAN")
    @NotNull
    private LoanType loanType;

    @Schema(description = "Principal amount to compute the EMI for", example = "500000")
    @NotNull
    @Positive
    private BigDecimal loanAmount;

    @Schema(description = "Repayment tenure in months", example = "60")
    @NotNull
    @Positive
    private Integer tenureMonths;
}
