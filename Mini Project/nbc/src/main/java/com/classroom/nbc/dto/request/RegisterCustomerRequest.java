package com.classroom.nbc.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Payload to register a new customer and their login account")
public class RegisterCustomerRequest {

    @Schema(description = "Customer's full name", example = "Jane Doe")
    @NotBlank
    private String name;

    @Schema(description = "Unique email address", example = "jane.doe@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Contact phone number", example = "9876543210")
    @NotBlank
    private String phone;

    @Schema(description = "Gross annual income used for eligibility", example = "850000")
    @NotNull
    @Positive
    private BigDecimal annualIncome;

    @Schema(description = "Unique login username", example = "jane.doe")
    @NotBlank
    private String username;

    @Schema(description = "Account password (minimum 6 characters)", example = "S3curePass",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank
    @Size(min = 6)
    private String password;

}
