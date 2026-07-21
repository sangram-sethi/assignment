package com.classroom.nbc.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Credentials for authentication")
public class LoginRequest {

    @Schema(description = "Login username", example = "jane.doe")
    @NotBlank
    private String username;

    @Schema(description = "Account password", example = "S3curePass",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank
    private String password;

}
