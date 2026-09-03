package com.orquestro.management.service;

import com.orquestro.data.domain.Language;
import com.orquestro.data.domain.User;
import com.orquestro.data.domain.UserRole;
import com.orquestro.data.domain.UserSession;
import com.orquestro.data.repository.LanguageRepository;
import com.orquestro.data.repository.UserRepository;
import com.orquestro.data.repository.UserRoleRepository;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for user authentication and authorization workflows.
 * Orchestrates login, registration, and session management using an indirect RBAC model.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final String DEFAULT_REGISTRATION_ROLE = "USER";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final LanguageRepository languageRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    /**
     * Authenticates a user and manages the security context.
     * 
     * @param request the login credentials.
     * @return the authentication details and security tokens.
     */
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            return processSuccessfulLogin(request.email());

        } catch (BadCredentialsException e) {
            updateFailedAttempts(request.email());
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        } catch (LockedException e) {
            throw new BusinessException("This account has been locked due to multiple failed login attempts.", HttpStatus.FORBIDDEN);
        } catch (DisabledException e) {
            throw new BusinessException("This account is currently inactive.", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Updates the counter of failed attempts in a dedicated transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isActive() && !user.isAccountLocked()) {
                user.incrementFailedAttempts();
                if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    user.setAccountLocked(true);
                }
                userRepository.save(user);
            }
        });
    }

    /**
     * Finalizes the login process upon success.
     */
    @Transactional
    public AuthenticationResponseDTO processSuccessfulLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found.", HttpStatus.NOT_FOUND));

        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return createSessionAndBuildResponse(user);
    }

    /**
     * Registers a new user and assigns a default access profile.
     * 
     * @param request the registration details.
     * @return the authentication details for the new user.
     */
    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email address already in use.", HttpStatus.CONFLICT);
        }

        Language language = languageRepository.findByCode(request.languageCode())
                .orElseGet(() -> languageRepository.findByIsDefaultTrue()
                        .orElseThrow(() -> new BusinessException("Default language not found.", HttpStatus.INTERNAL_SERVER_ERROR)));

        UserRole defaultRole = userRoleRepository.findByName(DEFAULT_REGISTRATION_ROLE)
                .orElseThrow(() -> new BusinessException("Default user role configuration not found.", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .language(language)
                .active(true)
                .accountLocked(false)
                .roles(Set.of(defaultRole))
                .build();

        User savedUser = userRepository.save(user);
        return createSessionAndBuildResponse(savedUser);
    }

    /**
     * Issues new tokens using a valid refresh token.
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

        session.setRevoked(true);
        userSessionRepository.save(session);

        User user = session.getUser();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = UUID.randomUUID().toString();

        saveUserSession(user, newRefreshToken);

        return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Revokes the user session.
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
     * Internal helper to create session records and build the response DTO.
     */
    private AuthenticationResponseDTO createSessionAndBuildResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        saveUserSession(user, refreshToken);

        Set<String> roleNames = user.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .build();
    }

    /**
     * Persists a new session in the database.
     */
    private void saveUserSession(User user, String refreshToken) {
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(1))
                .revoked(false)
                .build();
        userSessionRepository.save(session);
    }
}