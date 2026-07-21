package com.classroom.nbc.dto.request;

import com.classroom.nbc.enums.LoanStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Approval decision for a pending loan")
public class LoanApprovalRequest {

    @Schema(description = "Decision to apply to the loan", example = "APPROVED")
    @NotNull
    private LoanStatus status;

    @Schema(description = "Reviewer remarks explaining the decision", example = "Income and documents verified")
    @NotBlank
    private String remarks;

}
