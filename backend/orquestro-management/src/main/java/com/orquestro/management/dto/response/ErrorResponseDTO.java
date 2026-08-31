package com.orquestro.management.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized data transfer object for error responses across the platform.
 * Provides consistent error formatting for the frontend to consume.
 * 
 * @param timestamp The exact moment the error occurred.
 * @param status The HTTP status code value.
 * @param error The short name of the HTTP error.
 * @param message A user-friendly message explaining the error.
 * @param path The URI where the error originated.
 * @param validationErrors A list of specific field errors, if any (e.g., validation failures).
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Builder
public record ErrorResponseDTO(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationError> validationErrors
) {
    /**
     * Nested record to represent specific field validation failures.
     */
    public record ValidationError(String field, String message) {}
}