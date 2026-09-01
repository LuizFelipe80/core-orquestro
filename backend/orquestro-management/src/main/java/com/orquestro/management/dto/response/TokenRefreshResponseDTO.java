package com.orquestro.management.dto.response;

import lombok.Builder;

/**
 * Data Transfer Object returned after a successful token refresh operation.
 * Provides the new access token and a new refresh token (rotation).
 * 
 * @param accessToken The new short-lived JWT access token.
 * @param refreshToken The new refresh token to be used for the next rotation.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Builder
public record TokenRefreshResponseDTO(
    String accessToken,
    String refreshToken
) {
}