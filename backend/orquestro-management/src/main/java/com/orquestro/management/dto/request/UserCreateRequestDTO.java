package com.orquestro.management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object representing an administrative user creation request.
 * Allows administrators/managers to create users with explicit language and roles assignment.
 * 
 * @param firstName The user's first name.
 * @param lastName The user's last name.
 * @param email The user's unique email address.
 * @param password The user's plain text initial password.
 * @param languageId The UUID of the user's preferred language.
 * @param roles The set of role profile names to assign (e.g., ['USER', 'MANAGER']).
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserCreateRequestDTO(
    
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

    @NotNull(message = "Language ID is required")
    UUID languageId,

    Set<String> roles
) {
}
