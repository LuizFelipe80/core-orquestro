package com.orquestro.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for creating or updating a user access profile.
 * Allows mapping multiple module roles to a single high-level profile.
 * 
 * @param name The unique name for the profile.
 * @param description The profile's purpose description.
 * @param active Status of the profile.
 * @param moduleRoleIds A set of IDs for the granular roles to be mapped.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record UserRoleRequestDTO(
    @NotBlank(message = "Profile name is required")
    @Size(max = 100)
    String name,

    @Size(max = 255)
    String description,

    boolean active,

    Set<UUID> moduleRoleIds
) {
}