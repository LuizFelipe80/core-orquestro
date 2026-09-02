package com.orquestro.management.dto.response;

import java.util.UUID;

/**
 * Data Transfer Object representing an access grant for a user within the module.
 * Provides details about the user, their assigned module-specific role, and the status.
 * 
 * @param id The unique identifier of the access record.
 * @param userId The unique identifier of the user.
 * @param userName The full name of the user for UI display.
 * @param roleId The unique identifier of the assigned module role.
 * @param roleName The name of the module role (e.g., OEE_OPERATOR).
 * @param active Indicates if this specific access is currently active.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserModuleAccessResponseDTO(
    UUID id,
    UUID userId,
    String userName,
    UUID roleId,
    String roleName,
    boolean active
) {
}