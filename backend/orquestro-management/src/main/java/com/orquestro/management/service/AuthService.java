package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserSession;
import com.orquestro.data.domain.enums.UserRole;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.data.repository.UserSessionRepository;
import com.orquestro.management.dto.request.AuthenticationRequestDTO;
import com.orquestro.management.dto.request.RegisterRequestDTO;
import com.orquestro.management.dto.request.TokenRefreshRequestDTO;
import com.orquestro.management.dto.response.AuthenticationResponseDTO;
import com.orquestro.management.dto.response.TokenRefreshResponseDTO;
import com.orquestro.management.exception.BusinessException;
import com.orquestro.management.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsible for managing user authentication, registration, and session rotation.
 * It coordinates token generation with database-backed session management for enhanced security.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    /**
     * Registers a new user and initiates a secure session.
     */
    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email address already in use.", HttpStatus.CONFLICT);
        }

        Language language = languageRepository.findByCode(request.languageCode())
                .orElseGet(() -> languageRepository.findByIsDefaultTrue()
                        .orElseThrow(() -> new BusinessException("Default language not found.", HttpStatus.INTERNAL_SERVER_ERROR)));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .globalRole(UserRole.ROLE_USER)
                .language(language)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        return createSessionAndBuildResponse(savedUser);
    }

    /**
     * Authenticates credentials and creates a new tracked session.
     */
    @Transactional
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("User not found.", HttpStatus.NOT_FOUND));

        return createSessionAndBuildResponse(user);
    }

    /**
     * Performs Refresh Token Rotation.
     * Validates the old refresh token, revokes it, and issues a new pair of tokens.
     * This is a critical security measure to prevent replay attacks.
     */
    @Transactional
    public TokenRefreshResponseDTO refreshToken(TokenRefreshRequestDTO request) {
        UserSession session = userSessionRepository.findByRefreshTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid or revoked refresh token.", HttpStatus.UNAUTHORIZED));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setRevoked(true);
            userSessionRepository.save(session);
            throw new BusinessException("Refresh token has expired.", HttpStatus.UNAUTHORIZED);
        }

        /* Rotation: Revoke current session and issue a new one */
        session.setRevoked(true);
        userSessionRepository.save(session);

        User user = session.getUser();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = UUID.randomUUID().toString(); /* Using UUID for opaque Refresh Tokens */

        saveUserSession(user, newRefreshToken);

        return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Logs out the user by revoking the specific refresh token.
     */
    @Transactional
    public void logout(String refreshToken) {
        userSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    userSessionRepository.save(session);
                });
    }

    /**
     * Internal helper to create a session and map the full authentication response.
     */
    private AuthenticationResponseDTO createSessionAndBuildResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        saveUserSession(user, refreshToken);

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .globalRole(user.getGlobalRole().name())
                .build();
    }

    /**
     * Persists a new session in the database.
     */
    private void saveUserSession(User user, String refreshToken) {
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(1)) /* Standard 1-week expiration for Refresh Tokens */
                .revoked(false)
                .build();
        userSessionRepository.save(session);
    }
}