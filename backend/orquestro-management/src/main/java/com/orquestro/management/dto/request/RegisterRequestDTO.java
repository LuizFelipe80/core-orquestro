package com.orquestro.management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object representing a user registration request.
 * Contains necessary fields and validation constraints to create a new user 
 * in the Orquestro platform.
 * 
 * @param firstName The user's first name.
 * @param lastName The user's last name.
 * @param email The user's unique email address, used for login.
 * @param password The user's plain text password (will be encoded before storage).
 * @param languageCode The ISO code of the user's preferred language (e.g., 'en', 'pt-BR').
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record RegisterRequestDTO(
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 180, message = "Email must not exceed 180 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password,

    @NotBlank(message = "Language code is required")
    @Size(min = 2, max = 10, message = "Invalid language code format")
    String languageCode
) {
}