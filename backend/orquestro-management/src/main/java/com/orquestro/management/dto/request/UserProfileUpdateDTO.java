package com.orquestro.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Data Transfer Object for users to update their own profile information.
 * Restricts updates to non-sensitive fields to maintain system integrity.
 * 
 * @param firstName The user's first name.
 * @param lastName The user's last name.
 * @param languageId The preferred language identifier.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserProfileUpdateDTO(
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    String lastName,

    @NotNull(message = "Language is required")
    UUID languageId
) {
}