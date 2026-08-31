package com.orquestro.exception;

import com.orquestro.management.dto.response.ErrorResponseDTO;
import com.orquestro.management.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler that intercepts and processes all exceptions thrown across the application.
 * It ensures that the client always receives a standardized JSON response (ErrorResponseDTO)
 * instead of default server error pages or stack traces.
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
     * Maps each field error to a ValidationError record for frontend consumption.
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
     * Handles security authentication failures specifically.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    /**
     * Catch-all handler for any unexpected server errors.
     * Logs the full stack trace for internal debugging while returning a generic message to the client.
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