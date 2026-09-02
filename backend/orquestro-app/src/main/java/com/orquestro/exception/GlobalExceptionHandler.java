package com.orquestro.exception;

import com.orquestro.management.dto.response.ErrorResponseDTO;
import com.orquestro.management.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler that intercepts and processes all exceptions thrown across the application.
 * Updated to specifically handle security-related exceptions such as account locking and disabled users.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles custom business logic exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), request, null);
    }

    /**
     * Handles validation errors from DTOs (e.g., @NotBlank, @Email).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponseDTO.ValidationError> validationErrors = new ArrayList<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.add(new ErrorResponseDTO.ValidationError(fieldName, errorMessage));
        });

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields", request, validationErrors);
    }

    /**
     * Handles incorrect login credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    /**
     * Handles attempts to log in with a locked account (Brute Force Protection).
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponseDTO> handleLockedException(LockedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "This account has been locked due to multiple failed login attempts. Please contact support.", request, null);
    }

    /**
     * Handles attempts to log in with a disabled/inactive account.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponseDTO> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "This account is currently inactive.", request, null);
    }

    /**
     * Catch-all handler for any unexpected server errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("An unexpected error occurred at {}: ", request.getRequestURI(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", request, null);
    }

    /**
     * Internal helper method to construct the standardized ErrorResponseDTO.
     */
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(
            HttpStatus status, 
            String message, 
            HttpServletRequest request,
            List<ErrorResponseDTO.ValidationError> validationErrors
    ) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}