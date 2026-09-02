package com.orquestro.management.dto.response;

import java.util.UUID;

/**
 * Data Transfer Object representing a module-specific role.
 * 
 * @param id The unique identifier of the role.
 * @param name The role name (e.g., OEE_OPERATOR).
 * @param description A brief explanation of the role's purpose.
 * @param active Indicates if the role is available for assignment.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record ModuleRoleResponseDTO(
    UUID id,
    String name,
    String description,
    boolean active
) {
}