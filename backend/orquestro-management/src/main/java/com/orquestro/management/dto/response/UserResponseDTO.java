package com.orquestro.management.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object representing a detailed user profile.
 * Updated to support multiple UserRoles as part of the flexible RBAC model.
 * 
 * @param id The unique identifier of the user.
 * @param firstName The user's first name.
 * @param lastName The user's last name.
 * @param email The user's email address.
 * @param roles A set of role names assigned to the user (e.g., ADMINISTRATOR, MANAGER).
 * @param active Indicates if the account is currently enabled.
 * @param accountLocked Indicates if the account is blocked due to security policies.
 * @param lastLoginAt The last successful authentication timestamp.
 * @param createdAt The account creation timestamp.
 * @param language The user's preferred language summary.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Builder
public record UserResponseDTO(
    UUID id,
    String firstName,
    String lastName,
    String email,
    Set<String> roles,
    boolean active,
    boolean accountLocked,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LanguageSummaryDTO language
) {
    /**
     * Compact representation of the user's language for UI display.
     */
    public record LanguageSummaryDTO(UUID id, String name, String code) {}
}