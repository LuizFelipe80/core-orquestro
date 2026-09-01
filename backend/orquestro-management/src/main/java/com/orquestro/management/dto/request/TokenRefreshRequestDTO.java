package com.orquestro.management.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for requesting a new access token using a refresh token.
 * This is part of the security rotation mechanism to keep users authenticated 
 * without exposing long-lived access tokens.
 * 
 * @param refreshToken The valid, non-expired refresh token stored by the client.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
public record TokenRefreshRequestDTO(
    
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {
}