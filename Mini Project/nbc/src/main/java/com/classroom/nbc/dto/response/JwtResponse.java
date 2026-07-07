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
@Schema(description = "Authentication token and principal details")
public class JwtResponse {

    @Schema(description = "Signed JWT to send as a Bearer token")
    private String token;

    @Schema(description = "Authenticated username", example = "jane.doe")
    private String username;

    @Schema(description = "Role of the authenticated user", example = "CUSTOMER")
    private String role;

}
