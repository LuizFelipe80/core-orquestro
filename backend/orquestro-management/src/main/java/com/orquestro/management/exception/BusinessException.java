package com.orquestro.management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom exception class for handling business logic failures within the Orquestro platform.
 * This unchecked exception allows the service layer to signal business rule 
 * violations with a specific HTTP status code.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * The HTTP status code that should be returned to the client.
     */
    private final HttpStatus status;

    /**
     * Constructs a new BusinessException with a message and status.
     * 
     * @param message the detail message explaining the business error.
     * @param status the HTTP status associated with this error.
     */
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Constructs a new BusinessException with a message, defaulting to HTTP 400 Bad Request.
     * 
     * @param message the detail message explaining the business error.
     */
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}