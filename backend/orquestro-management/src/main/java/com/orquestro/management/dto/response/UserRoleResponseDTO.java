package com.orquestro.management.dto.response;

import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object representing a high-level access profile (UserRole).
 * Includes the collection of inherited granular ModuleRoles.
 * 
 * @param id The unique identifier of the user role.
 * @param name The role name (e.g., ADMINISTRATOR).
 * @param description A brief explanation of the profile's purpose.
 * @param active Indicates if the profile is available for assignment.
 * @param moduleRoles The set of inherited granular permissions.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserRoleResponseDTO(
    UUID id,
    String name,
    String description,
    boolean active,
    Set<ModuleRoleSummaryDTO> moduleRoles
) {
    /**
     * Compact representation of a module role for list displays.
     */
    public record ModuleRoleSummaryDTO(UUID id, String name) {}
}