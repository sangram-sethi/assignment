package com.classroom.nbc.dto.request;

import java.math.BigDecimal;

import com.classroom.nbc.enums.LoanType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Payload to apply for a new loan")
public class LoanApplicationRequest {

    @Schema(description = "Type of loan being requested", example = "HOME_LOAN")
    @NotNull
    private LoanType loanType;

    @Schema(description = "Requested principal amount", example = "500000")
    @NotNull
    @Positive
    private BigDecimal loanAmount;

    @Schema(description = "Repayment tenure in months", example = "60")
    @NotNull
    @Positive
    private Integer tenureMonths;

    @Schema(description = "Reason for the loan", example = "Home renovation")
    @NotBlank
    private String purpose;

}
