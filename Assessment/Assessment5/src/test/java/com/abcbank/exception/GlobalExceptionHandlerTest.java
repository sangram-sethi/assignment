package com.abcbank.exception;

import com.abcbank.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Executable specification for {@link GlobalExceptionHandler}, verifying the
 * exception-to-HTTP-status mapping required by the challenge:
 * <pre>
 *   CustomerNotFound -> 404   AccountNotFound -> 404   DuplicateEmail -> 409
 *   ValidationError  -> 400   Unauthorized    -> 401   Forbidden      -> 403
 * </pre>
 */
@DisplayName("GlobalExceptionHandler specification")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("maps CustomerNotFoundException to 404 Not Found")
    void customerNotFound_mapsTo404() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new CustomerNotFoundException("missing"));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("maps AccountNotFoundException to 404 Not Found")
    void accountNotFound_mapsTo404() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new AccountNotFoundException("missing"));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("maps DuplicateEmailException to 409 Conflict")
    void duplicateEmail_mapsTo409() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateEmail(new DuplicateEmailException("dup"));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
    }

    @Test
    @DisplayName("maps InsufficientBalanceException to 400 Bad Request")
    void insufficientBalance_mapsTo400() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(new InsufficientBalanceException("insufficient"));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("maps InvalidTransactionException to 400 Bad Request")
    void invalidTransaction_mapsTo400() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(new InvalidTransactionException("invalid"));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("maps validation errors to 400 Bad Request with per-field messages")
    void validationError_mapsTo400WithFieldErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        given(ex.getBindingResult()).willReturn(bindingResult);
        given(bindingResult.getFieldErrors()).willReturn(List.of(
                new FieldError("customerRequest", "email", "Email must be valid")));

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().fieldErrors()).containsEntry("email", "Email must be valid");
    }

    @Test
    @DisplayName("maps AuthenticationException to 401 Unauthorized")
    void authenticationError_mapsTo401() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleUnauthorized(new BadCredentialsException("bad credentials"));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().status()).isEqualTo(401);
    }

    @Test
    @DisplayName("maps AccessDeniedException to 403 Forbidden")
    void accessDenied_mapsTo403() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleForbidden(new AccessDeniedException("forbidden"));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().status()).isEqualTo(403);
    }
}
