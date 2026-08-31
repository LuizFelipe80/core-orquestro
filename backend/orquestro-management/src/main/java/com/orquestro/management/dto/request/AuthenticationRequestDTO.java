package com.orquestro.management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object representing a login request.
 * Carries the necessary credentials to authenticate a user within the platform.
 * 
 * @param email The user's registered email address.
 * @param password The user's plain text password to be verified.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record AuthenticationRequestDTO(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {
}