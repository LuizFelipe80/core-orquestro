package com.orquestro.management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Data Transfer Object representing a user update request.
 * Updated to use a String for the role name, supporting the dynamic 
 * Entity-based RBAC model.
 * 
 * @param firstName The user's updated first name.
 * @param lastName The user's updated last name.
 * @param email The user's updated email address.
 * @param globalRole The name of the role to be assigned (e.g., "ADMINISTRATOR").
 * @param languageId The unique identifier of the user's preferred language.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserUpdateDTO(
    
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

    @NotBlank(message = "Global role name is required")
    String globalRole,

    @NotNull(message = "Language ID is required")
    UUID languageId
) {
}