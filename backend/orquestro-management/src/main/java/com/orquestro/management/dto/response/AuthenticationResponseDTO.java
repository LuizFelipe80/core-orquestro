package com.orquestro.management.dto.response;

import lombok.Builder;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object representing a successful authentication response.
 * Updated to support multiple roles and the indirect RBAC model.
 * 
 * @param accessToken The short-lived JWT token.
 * @param refreshToken The long-lived token used to renew access.
 * @param userId The unique identifier of the authenticated user.
 * @param email The user's email address.
 * @param fullName The user's concatenated first and last name.
 * @param roles A set of high-level UserRole names assigned to the user.
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
    Set<String> roles
) {
}