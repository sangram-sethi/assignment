package com.classroom.nbc.dto.response;

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
@Schema(description = "Aggregate counts of a customer's loan applications by status")
public class LoanSummaryResponse {

    @Schema(description = "Total number of applications", example = "5")
    private long totalApplications;

    @Schema(description = "Number of approved applications", example = "2")
    private long approvedApplications;

    @Schema(description = "Number of rejected applications", example = "1")
    private long rejectedApplications;

    @Schema(description = "Number of pending applications", example = "2")
    private long pendingApplications;
}
