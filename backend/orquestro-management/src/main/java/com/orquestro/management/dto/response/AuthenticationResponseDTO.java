package com.orquestro.management.dto.response;

import lombok.Builder;
import java.util.UUID;

/**
 * Data Transfer Object representing a successful authentication response.
 * This record carries the JWT access token and basic user identification 
 * to be stored and used by the frontend application.
 * 
 * @param accessToken The JWT token to be used in the Authorization header.
 * @param userId The unique identifier of the authenticated user.
 * @param email The user's email address.
 * @param fullName The user's concatenated first and last name.
 * @param globalRole The user's global role in the Orquestro platform.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Builder
public record AuthenticationResponseDTO(
    String accessToken,
    UUID userId,
    String email,
    String fullName,
    String globalRole
) {
}