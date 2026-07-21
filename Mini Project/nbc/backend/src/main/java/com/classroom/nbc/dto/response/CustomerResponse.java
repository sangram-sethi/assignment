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
@Schema(description = "Customer profile details")
public class CustomerResponse {

    @Schema(description = "Customer identifier", example = "1")
    private Long id;

    @Schema(description = "Customer's full name", example = "Jane Doe")
    private String name;

    @Schema(description = "Email address", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Contact phone number", example = "9876543210")
    private String phone;

    @Schema(description = "Gross annual income", example = "850000")
    private BigDecimal annualIncome;

}
