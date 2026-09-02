package com.orquestro.management.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Data Transfer Object for granting or updating user access within the module.
 * 
 * @param userId The ID of the user to receive the access.
 * @param moduleRoleId The ID of the specific role to be assigned.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserModuleAccessRequestDTO(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotNull(message = "Module Role ID is required")
    UUID moduleRoleId
) {
}