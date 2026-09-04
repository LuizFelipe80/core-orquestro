package com.orquestro.management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object representing a user update request.
 * Updated to support multiple roles, allowing for permission aggregation.
 * 
 * @param firstName The user's updated first name.
 * @param lastName The user's updated last name.
 * @param email The user's updated email address.
 * @param roles A set of role names to be assigned (e.g., ["ADMINISTRATOR", "MANAGER"]).
 * @param languageId The unique identifier of the user's preferred language.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserUpdateDTO(
    
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 180)
    String email,

    @NotEmpty(message = "At least one role must be assigned")
    Set<String> roles,

    @NotNull(message = "Language ID is required")
    UUID languageId
) {
}