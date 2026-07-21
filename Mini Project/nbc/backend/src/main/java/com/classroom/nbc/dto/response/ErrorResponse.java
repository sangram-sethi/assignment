package com.classroom.nbc.dto.response;

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
@Schema(description = "Standard error payload returned for failed requests")
public class ErrorResponse {

    @Schema(description = "When the error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP reason phrase", example = "Not Found")
    private String error;

    @Schema(description = "Error detail message or list of validation messages")
    private Object message;

    @Schema(description = "Request path that produced the error", example = "/api/loans/99")
    private String path;
}
