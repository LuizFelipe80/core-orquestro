package com.orquestro.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating or updating a module-specific role.
 * 
 * @param name The unique name for the role.
 * @param description Information about the role's permissions.
 * @param active The status of the role.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record ModuleRoleRequestDTO(
    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    boolean active
) {
}