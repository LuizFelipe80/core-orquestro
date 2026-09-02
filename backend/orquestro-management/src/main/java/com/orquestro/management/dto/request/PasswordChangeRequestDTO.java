package com.orquestro.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for secure password change operations.
 * Requires the current password to verify user identity before applying changes.
 * 
 * @param currentPassword The user's existing password.
 * @param newPassword The new password to be set.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record PasswordChangeRequestDTO(
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    String newPassword
) {
}