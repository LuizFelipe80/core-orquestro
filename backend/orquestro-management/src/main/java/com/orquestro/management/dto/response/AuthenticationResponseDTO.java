package com.orquestro.management.dto.response;

import lombok.Builder;
import java.util.UUID;

/**
 * Data Transfer Object representing a successful authentication response.
 * Updated to include the refresh token for session rotation.
 * 
 * @param accessToken The short-lived JWT token.
 * @param refreshToken The long-lived token used to renew access.
 * @param userId The unique identifier of the authenticated user.
 * @param email The user's email address.
 * @param fullName The user's concatenated first and last name.
 * @param globalRole The user's global role.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Builder
public record AuthenticationResponseDTO(
    String accessToken,
    String refreshToken,
    UUID userId,
    String email,
    String fullName,
    String globalRole
) {
}