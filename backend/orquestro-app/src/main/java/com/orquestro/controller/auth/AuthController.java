package com.orquestro.controller.auth;

import com.orquestro.management.dto.request.AuthenticationRequestDTO;
import com.orquestro.management.dto.request.RegisterRequestDTO;
import com.orquestro.management.dto.request.TokenRefreshRequestDTO;
import com.orquestro.management.dto.response.AuthenticationResponseDTO;
import com.orquestro.management.dto.response.TokenRefreshResponseDTO;
import com.orquestro.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication, registration, and session management.
 * Provides endpoints for users to enter the platform, refresh their access, 
 * and securely log out.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user. Access restricted to administrators in SecurityConfig.
     * 
     * @param request the registration details.
     * @return authentication details with both access and refresh tokens.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates a user and starts a new secure session.
     * 
     * @param request the login credentials.
     * @return authentication details with both access and refresh tokens.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(
            @Valid @RequestBody AuthenticationRequestDTO request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    /**
     * Renews an expired access token using a valid refresh token.
     * Implements token rotation for enhanced security.
     * 
     * @param request the refresh token.
     * @return a new pair of access and refresh tokens.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDTO> refresh(
            @Valid @RequestBody TokenRefreshRequestDTO request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Terminates a user session by revoking the provided refresh token.
     * 
     * @param request the refresh token to be invalidated.
     * @return 204 No Content on success.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody TokenRefreshRequestDTO request
    ) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}