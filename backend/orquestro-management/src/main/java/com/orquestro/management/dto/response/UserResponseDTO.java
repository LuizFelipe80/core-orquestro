package com.orquestro.management.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object representing a detailed user profile for management purposes.
 * Provides a comprehensive view of the user's identity, role, and system preferences.
 * 
 * @param id The unique identifier of the user.
 * @param firstName The user's first name.
 * @param lastName The user's last name.
 * @param email The user's email address.
 * @param globalRole The administrative role assigned to the user.
 * @param active Indicates if the account is currently enabled.
 * @param lastLoginAt The last time the user successfully authenticated.
 * @param createdAt The date and time the account was created.
 * @param language The user's preferred language details.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Builder
public record UserResponseDTO(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String globalRole,
    boolean active,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LanguageSummaryDTO language
) {
    /**
     * Compact representation of the user's language for UI display.
     */
    public record LanguageSummaryDTO(UUID id, String name, String code) {}
}